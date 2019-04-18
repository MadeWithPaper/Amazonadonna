import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import { ddb, s3 } from '../server'

const router = Router()

router.post('/getByAmznId', (req: Request, res: Response) => {
    const getByAmnzIdParams: aws.DynamoDB.Types.GetItemInput = {
        TableName: 'cga',
        Key: { cgaId: { S: req.body.amznId } }
    }
    ddb.getItem(getByAmnzIdParams, (err, data) => {
        if (err) {
            const msg = 'Error getting cga in cga/getByAmnzId: '
            console.log(msg + err)
            res.status(400).send(msg + err.message)
        } else {
            res.json(aws.DynamoDB.Converter.unmarshall(data.Item))
        }
    })
})

router.post('/add', (req: Request, res: Response) => {
    const putCgaParams: aws.DynamoDB.PutItemInput = {
        TableName: 'cga',
        Item: {
            cgaId: { S: req.body.amznId },
            city: { S: req.body.city },
            country: { S: req.body.country },
            name: { S: req.body.name },
            lat: { N: req.body.lat },
            lon: { N: req.body.lon }
        },
        ConditionExpression: 'attribute_not_exists(amznId)'
    }
    ddb.putItem(putCgaParams, (err, data) => {
        if (err) {
            const msg = 'Error adding cga in cga/add: '
            console.log(msg, err)
            res.status(400).send(msg + err.message)
        } else {
            res.send('Success!')
        }
    })
})

export { router as cgaRouter }
