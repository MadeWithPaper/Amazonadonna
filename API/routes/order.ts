import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import { ddb } from '../server'
import { OrderItem } from '../models/orderItem'
import { unmarshUtil } from '../utilities/unmarshall'

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
            const msg = 'Error fetching orders in order/listAll: '
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

router.post('/add', (req: Request, res: Response) => {
    const params: aws.DynamoDB.PutItemInput = {
        TableName: 'order',
        Item: {
            orderId: { S: req.body.orderId },
            cgoId: { S: req.body.cgoId },
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
            res.send('Successfully added')
        }
    })
})

router.post('/getItems', (req: Request, res: Response) => {
    const getItemsParams: aws.DynamoDB.Types.QueryInput = {
        TableName: 'orderItem',
        IndexName: 'orderId-index',
        KeyConditionExpression: 'orderId = :id',
        ExpressionAttributeValues: {
            ':id': { N: req.body.orderId }
        }
    }
    ddb.query(getItemsParams, (err, data) => {
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
                            Key: { itemId: { N: item.itemId.toString() } }
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
        Key: { orderId: { N: req.body.orderId } },
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

export { router as orderRouter }
