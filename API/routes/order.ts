import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import { ddb } from '../server'
import { OrderItem } from '../models/orderItem'
import { Item } from '../models/item'
import { unmarshUtil } from '../utilities/unmarshall'
import * as uuid from 'uuid'
import * as _ from 'lodash'

const router = Router()

router.post('/listAllForCga', (req: Request, res: Response) => {
    const listAllOrdersParams: aws.DynamoDB.Types.QueryInput = {
        TableName: 'order',
        IndexName: 'cgaId-index',
        KeyConditionExpression: 'cgaId = :id',
        ExpressionAttributeValues: {
            ':id': { S: req.body.cgaId }
        }
    }
    ddb.query(listAllOrdersParams, (err, data) => {
        if (err) {
            const msg = 'Error fetching orders in order/listAllForCga: '
            console.log(msg + err)
            res.status(400).send(msg + err.message)
        } else {
            const convert = unmarshUtil(data.Items)
            Promise.all(convert).then(items => {
                res.json(items)
            })
        }
    })
})

router.post('/listAllForArtisan', (req: Request, res: Response) => {
    const artisanId = req.body.artisanId

    const listAllItemsForOrderParams: aws.DynamoDB.Types.QueryInput = {
        TableName: 'item',
        IndexName: 'artisanId-index',
        KeyConditionExpression: 'artisanId = :id',
        ExpressionAttributeValues: {
            ':id': { S: artisanId }
        }
    }

    ddb.query(listAllItemsForOrderParams, (err, data) => {
        if (err) {
            console.log(
                'Error fetching items in item/listAllForArtisan: ' + err
            )
            res.status(400).send(
                'Error fetching artisans in item/listAllForArtisan: ' +
                    err.message
            )
        } else {
            const convert = unmarshUtil(data.Items)
            Promise.all(convert).then(items => {
                const ordersQuery = items.map((item: Item) => {
                    return new Promise(resolve => {
                        const getOrderItemParam: aws.DynamoDB.Types.QueryInput = {
                            TableName: 'orderItem',
                            IndexName: 'itemId-index',
                            KeyConditionExpression: 'itemId = :id',
                            ExpressionAttributeValues: {
                                ':id': { S: item.itemId }
                            }
                        }
                        ddb.query(
                            getOrderItemParam,
                            (orderItemErr, orderItemData) => {
                                if (orderItemErr) {
                                    console.log(
                                        'Error fetching orderItem in order/listAllForArtisan: ' +
                                            err
                                    )
                                    res.status(400).send(
                                        'Error fetching orderItem in order/listAllForArtisan: ' +
                                            err.message
                                    )
                                } else {
                                    resolve(orderItemData)
                                }
                            }
                        )
                    })
                })

                Promise.all(ordersQuery).then(
                    (
                        orderItemMarshalledItems: aws.DynamoDB.Types.QueryOutput[]
                    ) => {
                        const convertOrderItems = orderItemMarshalledItems.map(
                            orderItemMarshalledItem => {
                                return new Promise(resolve => {
                                    const orderItemsConvert = unmarshUtil(
                                        orderItemMarshalledItem.Items
                                    )
                                    Promise.all(orderItemsConvert).then(
                                        orderItemConvert => {
                                            resolve(orderItemConvert)
                                        }
                                    )
                                })
                            }
                        )
                        Promise.all(convertOrderItems).then(
                            (orderItems: OrderItem[][]) => {
                                const queryOrders = orderItems.map(
                                    orderItem => {
                                        if (orderItem.length === 1) {
                                            const singleOrderItem = orderItem[0]
                                            return new Promise(resolve => {
                                                const getOrderParams: aws.DynamoDB.Types.GetItemInput = {
                                                    TableName: 'order',
                                                    Key: {
                                                        orderId: {
                                                            S:
                                                                singleOrderItem.orderId
                                                        }
                                                    }
                                                }

                                                ddb.getItem(
                                                    getOrderParams,
                                                    (
                                                        getOrderErr,
                                                        getOrderData: aws.DynamoDB.Types.GetItemOutput
                                                    ) => {
                                                        if (getOrderErr) {
                                                            console.log(
                                                                'Error fetching orders in order/listAllForArtisan/getOrder: ' +
                                                                    getOrderErr
                                                            )
                                                            res.status(
                                                                400
                                                            ).send(
                                                                'Error fetching orders in order/listAllForArtisan/getOrder: ' +
                                                                    getOrderErr.message
                                                            )
                                                        } else {
                                                            resolve(
                                                                getOrderData
                                                            )
                                                        }
                                                    }
                                                )
                                            })
                                        } else {
                                            return null
                                        }
                                    }
                                )
                                Promise.all(queryOrders).then(
                                    (
                                        marshallItems: aws.DynamoDB.Types.GetItemOutput[]
                                    ) => {
                                        const convertItems = marshallItems.map(
                                            marshallItem => {
                                                return new Promise(resolve => {
                                                    const unmarshedItem = aws.DynamoDB.Converter.unmarshall(
                                                        marshallItem.Item
                                                    )
                                                    resolve(unmarshedItem)
                                                })
                                            }
                                        )
                                        Promise.all(convertItems).then(
                                            orderData => {
                                                res.json(
                                                    _.uniqBy(
                                                        orderData,
                                                        'orderId'
                                                    )
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            })
        }
    })
})

router.post('/add', (req: Request, res: Response) => {
    const id = uuid.v1()
    const params: aws.DynamoDB.PutItemInput = {
        TableName: 'order',
        Item: {
            orderId: { S: id },
            cgaId: { S: req.body.cgaId },
            shippedStatus: { BOOL: req.body.shippedStatus },
            numItems: { N: req.body.numItems },
            shippingAddress: { S: req.body.shippingAddress },
            totalCostDollars: { N: req.body.totalCostDollars },
            totalCostCents: { N: req.body.totalCostCents }
        }
    }
    ddb.putItem(params, (err, data) => {
        if (err) {
            console.log('Error adding order in order/add: ', err)
            res.status(400).send(
                'Error adding order in order/add: ' + err.message
            )
        } else {
            res.json(id.toString())
            /*sendText('19169903748', req.body.orderId)*/
        }
    })
})

router.post('/getItems', (req: Request, res: Response) => {
    const getParamsFromItems: aws.DynamoDB.Types.QueryInput = {
        TableName: 'orderItem',
        IndexName: 'orderId-index',
        KeyConditionExpression: 'orderId = :id',
        ExpressionAttributeValues: {
            ':id': { S: req.body.orderId }
        }
    }
    ddb.query(getParamsFromItems, (err, data) => {
        if (err) {
            console.log('Error fetching orderItem in order/getItems: ' + err)
            res.status(400).send(
                'Error fetching orderItem in order/getItems: ' + err.message
            )
        } else {
            const convert = unmarshUtil(data.Items)
            Promise.all(convert).then((items: OrderItem[]) => {
                const queryItems = items.map(item => {
                    return new Promise(resolve => {
                        const getItemParams: aws.DynamoDB.Types.GetItemInput = {
                            TableName: 'item',
                            Key: { itemId: { S: item.itemId } }
                        }
                        ddb.getItem(
                            getItemParams,
                            (
                                getItemErr,
                                getItemData: aws.DynamoDB.Types.GetItemOutput
                            ) => {
                                if (getItemErr) {
                                    console.log(
                                        'Error fetching items in order/getItems/getItem: ' +
                                            getItemErr
                                    )
                                    res.status(400).send(
                                        'Error fetching items in order/getItems/getItem: ' +
                                            getItemErr.message
                                    )
                                } else {
                                    resolve(getItemData)
                                }
                            }
                        )
                    })
                })
                Promise.all(queryItems).then(
                    (marshallItems: aws.DynamoDB.Types.GetItemOutput[]) => {
                        const convertItems = marshallItems.map(marshallItem => {
                            return new Promise(resolve => {
                                const unmarshedItem = aws.DynamoDB.Converter.unmarshall(
                                    marshallItem.Item
                                )
                                resolve(unmarshedItem)
                            })
                        })
                        Promise.all(convertItems).then(itemData => {
                            res.json(itemData)
                        })
                    }
                )
            })
        }
    })
})

router.post('/getItemsForArtisan', (req: Request, res: Response) => {
    if (req.body.artisanId === undefined) {
        const msg = 'Must provide artisanId in order/getItemsForArtisan'
        console.log(msg)
        res.status(400).send(msg)
    } else {
        const artisanId = req.body.artisanId
        const getParamsFromItems: aws.DynamoDB.Types.QueryInput = {
            TableName: 'orderItem',
            IndexName: 'orderId-index',
            KeyConditionExpression: 'orderId = :id',
            ExpressionAttributeValues: {
                ':id': { S: req.body.orderId }
            }
        }
        ddb.query(getParamsFromItems, (err, data) => {
            if (err) {
                console.log(
                    'Error fetching orderItem in order/getItems: ' + err
                )
                res.status(400).send(
                    'Error fetching orderItem in order/getItems: ' + err.message
                )
            } else {
                const convert = unmarshUtil(data.Items)
                Promise.all(convert).then((items: OrderItem[]) => {
                    const queryItems = items.map(item => {
                        return new Promise(resolve => {
                            const getItemParams: aws.DynamoDB.Types.GetItemInput = {
                                TableName: 'item',
                                Key: { itemId: { S: item.itemId } }
                            }
                            ddb.getItem(
                                getItemParams,
                                (
                                    getItemErr,
                                    getItemData: aws.DynamoDB.Types.GetItemOutput
                                ) => {
                                    if (getItemErr) {
                                        console.log(
                                            'Error fetching items in order/getItems/getItem: ' +
                                                getItemErr
                                        )
                                        res.status(400).send(
                                            'Error fetching items in order/getItems/getItem: ' +
                                                getItemErr.message
                                        )
                                    } else {
                                        resolve(getItemData)
                                    }
                                }
                            )
                        })
                    })
                    Promise.all(queryItems).then(
                        (marshallItems: aws.DynamoDB.Types.GetItemOutput[]) => {
                            const convertItems = marshallItems.map(
                                marshallItem => {
                                    return new Promise(resolve => {
                                        const unmarshedItem = aws.DynamoDB.Converter.unmarshall(
                                            marshallItem.Item
                                        )
                                        if (
                                            unmarshedItem.artisanId ===
                                            artisanId
                                        ) {
                                            resolve(unmarshedItem)
                                        } else {
                                            resolve({})
                                        }
                                    })
                                }
                            )
                            Promise.all(convertItems).then(itemData => {
                                res.json(
                                    itemData.filter(
                                        value => Object.keys(value).length !== 0
                                    )
                                )
                            })
                        }
                    )
                })
            }
        })
    }
})

router.post('/setShippedStatus', (req: Request, res: Response) => {
    const shippedBool = req.body.shippedStatus === 'true'
    const negShippedBool = req.body.shippedStatus === 'false'
    if (!shippedBool && !negShippedBool) {
        const msg = 'Error updating shipped status in order/setShippedStatus: '
        const err = 'shippedStatus key is not true or false or is missing'
        console.log(msg + err)
        res.status(400).send(msg + err)
    } else {
        const setShippedStatusParams: aws.DynamoDB.Types.UpdateItemInput = {
            TableName: 'order',
            Key: { orderId: { S: req.body.orderId } },
            UpdateExpression: 'set shippedStatus = :u',
            ExpressionAttributeValues: {
                ':u': { BOOL: shippedBool }
            },
            ReturnValues: 'UPDATED_NEW',
            ConditionExpression: 'attribute_exists(orderId)'
        }
        ddb.updateItem(setShippedStatusParams, (err, data) => {
            if (err) {
                console.log(
                    'Error updating shipped status in order/setShippedStatus: ' +
                        err
                )
                res.status(400).send(
                    'Error updating shipped status in order/setShippedStatus: ' +
                        err.message
                )
            } else {
                res.send('Success!')
            }
        })
    }
})

function sendText(phoneNumber: string, orderId: string) {
    const sns = new aws.SNS()
    const params: aws.SNS.PublishInput = {
        Message: 'Order Number' + orderId + 'Received',
        PhoneNumber: phoneNumber
    }
    sns.publish(params, (err, data) => {
        if (err) {
            console.log(
                'Error sending text to ' +
                    params.PhoneNumber +
                    'for orderId' +
                    orderId
            )
            console.log(err, err.stack)
        }
        // an error occurred
        else {
            console.log('Sent text message') // successful response
        }
    })
}

export { router as orderRouter }
