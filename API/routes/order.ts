import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import { ddb } from '../server'
import { OrderItem } from '../models/orderItem'
import { unmarshUtil } from '../utilities/unmarshall'
import * as uuid from 'uuid'
import { Cgo } from '../models/cgo'
import * as MwsApi from 'amazon-mws'
import { MwsOrderRes } from '../models/mwsOrderRes'

const router = Router()

router.post('/listAllForCgo', (req: Request, res: Response) => {
    const listAllOrdersParams: aws.DynamoDB.Types.QueryInput = {
        TableName: 'order',
        IndexName: 'cgoId-index',
        KeyConditionExpression: 'cgoId = :id',
        ExpressionAttributeValues: {
            ':id': { S: req.body.cgoId }
        }
    }
    ddb.query(listAllOrdersParams, (err, data) => {
        if (err) {
            const msg = 'Error fetching orders in order/listAllForCgo: '
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

router.post('/setShippedStatus', (req: Request, res: Response) => {
    const setShippedStatusParams: aws.DynamoDB.Types.UpdateItemInput = {
        TableName: 'order',
        Key: { orderId: { S: req.body.orderId } },
        UpdateExpression: 'set shippedStatus = :u',
        ExpressionAttributeValues: {
            ':u': { BOOL: req.body.shippedStatus }
        },
        ReturnValues: 'UPDATED_NEW'
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
})

router.post('/tryUpdate', (req: Request, res: Response) => {
    const getCgoParams: aws.DynamoDB.Types.GetItemInput = {
        TableName: 'cgo',
        Key: { cgoId: { S: req.body.amznId } }
    }

    ddb.getItem(getCgoParams, (err, data) => {
        if (err) {
            const msg = 'Error getting cgo in order/tryUpdate: '
            console.log(msg + err)
            res.status(400).send(msg + err.message)
        } else {
            const cgo: Cgo = aws.DynamoDB.Converter.unmarshall(data.Item) as Cgo
            if (Date.now() - cgo.lastUpdateOrders > 15000) {
                // test
                const amazonMws = new MwsApi()
                amazonMws.setApiKey(cgo.mwsKey, cgo.mwsSecret)
                const lastUpdate = new Date(0)
                lastUpdate.setUTCMilliseconds(cgo.lastUpdateOrders)

                const mwsOrdersParams = {
                    Version: '2013-09-01',
                    Action: 'ListOrders',
                    SellerId: cgo.mwsSellerId,
                    MWSAuthToken: cgo.mwsAuthToken,
                    'MarketplaceId.Id.1': 'A1AM78C64UM0Y8', // Amazon.com.mx
                    CreatedAfter: lastUpdate.toISOString
                }

                amazonMws.orders.search(mwsOrdersParams).then(
                    (orders: MwsOrderRes) => {
                        const ordersToAdd = orders.Orders.map(order => {
                            return new Promise(resolve => {
                                const id = uuid.v1()
                                const params: aws.DynamoDB.PutItemInput = {
                                    TableName: 'order',
                                    Item: {
                                        orderId: { S: id },
                                        amazonOrderId: {
                                            S: order.AmazonOrderId
                                        },
                                        cgoId: { S: req.body.amznId },
                                        shippedStatus: {
                                            BOOL: false
                                        },
                                        numItems: {
                                            N: (
                                                order.NumberOfItemsShipped +
                                                order.NumberOfItemsUnshipped
                                            ).toString()
                                        },
                                        shippingAddress: {
                                            S:
                                                order.ShippingAddress
                                                    .AddressLine1
                                        },
                                        totalCostDollars: {
                                            N: order.OrderTotal.Amount.split(
                                                '.'
                                            )[0].toString()
                                        },
                                        totalCostCents: {
                                            N: order.OrderTotal.Amount.split(
                                                '.'
                                            )[1].toString()
                                        }
                                    }
                                }
                                ddb.putItem(
                                    params,
                                    (addOrderErr, addOrderData) => {
                                        if (addOrderErr) {
                                            const msg =
                                                'Error adding order in order/tryUpdate: '
                                            console.log(msg + err)
                                            res.status(400).send(
                                                msg + err.message
                                            )
                                        } else {
                                            // get items for order
                                            const mwsOrderItemParams = {
                                                Version: '2013-09-01',
                                                Action: 'ListOrderItems',
                                                SellerId: cgo.mwsSellerId,
                                                MWSAuthToken: cgo.mwsAuthToken,
                                                AmazonOrderId:
                                                    order.AmazonOrderId
                                            }

                                            amazonMws.orders
                                                .search(mwsOrderItemParams)
                                                .then(
                                                    items => {
                                                        // items
                                                        // go through each and
                                                        // search our items via
                                                        // the new indexer for
                                                        // amazon item id. Then
                                                        // add an entry in order
                                                        // item table. Thats it
                                                        // resolve()
                                                    },
                                                    mwsOrderItemErr => {
                                                        const msg =
                                                            'Error getting order items from mws in order/tryUpdate: '
                                                        console.log(
                                                            msg +
                                                                mwsOrderItemErr
                                                        )
                                                        res.status(400).send(
                                                            msg +
                                                                mwsOrderItemErr
                                                        )
                                                    }
                                                )
                                        }
                                    }
                                )
                            })
                        })
                        Promise.all(ordersToAdd).then(() => {
                            const updateLastUpdateParam: aws.DynamoDB.Types.UpdateItemInput = {
                                TableName: 'cgo',
                                Key: { cgoId: { S: req.body.amznId } },
                                UpdateExpression:
                                    'set lastUpdateOrders = :time',
                                ExpressionAttributeValues: {
                                    ':time': { S: Date.now().toString() }
                                },
                                ReturnValues: 'UPDATED_NEW'
                            }
                            ddb.updateItem(
                                updateLastUpdateParam,
                                (updateErr, updateData) => {
                                    if (updateErr) {
                                        const msg =
                                            'Error updating cgo lastUpdateOrder in order/tryUpdate: '
                                        console.log(msg + updateErr)
                                        res.status(400).send(
                                            msg + updateErr.message
                                        )
                                    } else {
                                        res.send('Updated!')
                                    }
                                }
                            )
                        })
                    },
                    mwsErr => {
                        const msg =
                            'Error getting orders from mws in order/tryUpdate: '
                        console.log(msg + mwsErr)
                        res.status(400).send(msg + mwsErr)
                    }
                )
            } else {
                res.send('Too soon')
            }
        }
    })
})

export { router as orderRouter }
