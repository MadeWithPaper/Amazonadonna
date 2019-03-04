import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import { ddb, s3 } from '../server'

const router = Router()

router.post('/getByAmznId', (req: Request, res: Response) => {
    const getByAmnzIdParams: aws.DynamoDB.Types.GetItemInput = {
        TableName: 'cgo',
        Key: { cgoId: { S: req.body.amznId } }
    }
    ddb.getItem(getByAmnzIdParams, (err, data) => {
        if (err) {
            const msg = 'Error getting cgo in cgo/getByAmnzId: '
            console.log(msg + err)
            res.status(400).send(msg + err.message)
        } else {
            res.json(aws.DynamoDB.Converter.unmarshall(data.Item))
        }
    })
})

router.post('/add', (req: Request, res: Response) => {
    const putCgoParams: aws.DynamoDB.PutItemInput = {
        TableName: 'cgo',
        Item: {
            cgoId: { S: req.body.amznId },
            city: { S: req.body.city },
            country: { S: req.body.country },
            name: { S: req.body.name },
            lat: { N: req.body.lat },
            lon: { N: req.body.lon },
            mwsKey: { S: req.body.mwsKey },
            mwsSecret: { S: req.body.mwsSecret },
            mwsAuthToken: { S: req.body.mwsAuthToken },
            mwsSellerId: { S: req.body.mwsSellerId },
            lastUpdateOrders: { N: '1' }
        }
    }
    ddb.putItem(putCgoParams, (err, data) => {
        if (err) {
            const msg = 'Error adding cgo in cgo/add: '
            console.log(msg, err)
            res.status(400).send(msg + err.message)
        } else {
            res.send('Success!')
        }
    })
})

export { router as cgoRouter }
