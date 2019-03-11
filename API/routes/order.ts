import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import { ddb } from '../server'
import { OrderItem } from '../models/orderItem'
import { unmarshUtil } from '../utilities/unmarshall'
import * as uuid from 'uuid'

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

router.post('/add', (req: Request, res: Response) => {
    const id = uuid.v1()
    const params: aws.DynamoDB.PutItemInput = {
        TableName: 'order',
        Item: {
            orderId: { S: id },
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
