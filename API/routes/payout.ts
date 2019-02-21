import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import * as multer from 'multer'
import * as multerS3 from 'multer-s3'
import * as mime from 'mime'
import { ddb, s3 } from '../server'
import { Payout } from '../models/payout'
import { unmarshUtil } from '../utilities/unmarshall'
import * as uuid from 'uuid'

const router = Router()

router.post('/listAllForArtisan', (req: Request, res: Response) => {
    const listAllPayoutsParams: aws.DynamoDB.Types.QueryInput = {
        TableName: 'payout',
        IndexName: 'artisanId-index',
        KeyConditionExpression: 'artisanId = :id',
        ExpressionAttributeValues: {
            ':id': { S: req.body.artisanId }
        }
    }
    ddb.query(listAllPayoutsParams, (err, data) => {
        if (err) {
            const msg = 'Error fetching payots in payout/listAllForArtisan'
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
        TableName: 'payout',
        Item: {
            payoutId: { S: id },
            artisanId: { S: req.body.artisanId },
            cgoId: { S: req.body.cgoId },
            amount: { N: req.body.amount },
            /*Date values are stored as ISO-8601 formatted strings*/
            date: { S: req.body.date },
            signaturePicURL: { S: 'Not set' }
        }
    }
    ddb.putItem(params, (err, data) => {
        if (err) {
            console.log('Error adding order in payout/add: ', err)
            res.status(400).send(
                'Error adding order in payout/add: ' + err.message
            )
        } else {
            res.json(id)
        }
    })
})

router.post('/updateImage', (req: Request, res: Response) => {
    // setup pic uploader with payoutId as filename
    const PayoutSignatureUploader = multer({
        storage: multerS3({
            s3,
            bucket: 'payout-signatures',
            acl: 'public-read',
            contentType: (picReq, file, cb) => {
                cb(null, file.mimetype)
            },
            metadata: (picReq, file, cb) => {
                cb(null, { fieldName: file.fieldname })
            },
            key: (picReq, file, cb) => {
                cb(
                    null,
                    req.body.payoutId + '.' + mime.getExtension(file.mimetype)
                )
            }
        })
    })

    const singleSignatureUpload = PayoutSignatureUploader.single('image')

    // upload pic
    singleSignatureUpload(req, res, picErr => {
        if (picErr) {
            const msg = 'Error uploading picture in payout/updateImage'
            console.log(msg, picErr)
            res.status(422).send(msg + picErr.message)
        } else {
            let signaturePicURL = 'Error: no picture attached'
            if (req.file) {
                signaturePicURL = (req.file as any).location
            }

            // update db record with new URL
            const params: aws.DynamoDB.UpdateItemInput = {
                TableName: 'payout',
                Key: { payoutId: { S: req.body.payoutId } },
                UpdateExpression: 'set signaturePicURL = :u',
                ExpressionAttributeValues: { ':u': { S: signaturePicURL } },
                ReturnValues: 'UPDATED_NEW'
            }
            // check string, params
            ddb.updateItem(params, (err, data) => {
                if (err) {
                    const msg = 'Error updating payout record '
                    console.log(msg + req.body.payoutId + ' : ' + err)
                    res.status(400).send(
                        msg + req.body.payoutId + ' : ' + err.message
                    )
                } else {
                    res.json({ imageUrl: signaturePicURL })
                }
            })
        }
    })
})

export { router as payoutRouter }
