import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import * as multer from 'multer'
import * as multerS3 from 'multer-s3'
import * as mime from 'mime'
import { ddb, s3 } from '../server'
import { OrderItem } from '../models/orderItem'

const router = Router()

router.get('/listAll', (req: Request, res: Response) => {
    const listAllOrdersParams: aws.DynamoDB.Types.QueryInput = {
        TableName: 'order',
        IndexName: 'cgoId-index',
        KeyConditionExpression: 'cgoId = :id',
        ExpressionAttributeValues: {
            ':id': { S: '0' }
        }
    }
    ddb.query(listAllOrdersParams, (err, data) => {
        if (err) {
            console.log('Error fetching orders in order/listAll: ' + err)
            res.status(400).send(
                'Error fetching orders in order/listAll: ' + err.message
            )
        } else {
            const convert = data.Items.map(item => {
                return new Promise(resolve => {
                    const unmarshed = aws.DynamoDB.Converter.unmarshall(item)
                    resolve(unmarshed)
                })
            })
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
            orderId: { S: req.body.orderd },
            cgoId: { S: req.body.cgoId },
            shippedStatus: { BOOL: req.body.shippedStatus },
            numItems: { N: req.body.numItems },
            shippingAddress: { S: req.body.shippingAddress },
            totalCostDollars: { N: req.body.totalCostDollars },
            totalCostCents: { N: req.body.totalCostCents }
            /*products: {List<Product>}*/
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
            const convert = data.Items.map(item => {
                return new Promise(resolve => {
                    const unmarshed = aws.DynamoDB.Converter.unmarshall(item)
                    resolve(unmarshed)
                })
            })
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

export { router as orderRouter } /*where to update orderRouter*/
