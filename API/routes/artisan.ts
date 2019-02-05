import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import * as multer from 'multer'
import * as multerS3 from 'multer-s3'
import * as mime from 'mime'
import { ddb, s3 } from '../server'

const router = Router()

const listAllArtisansParams: aws.DynamoDB.Types.QueryInput = {
    TableName: 'artisan',
    IndexName: 'cgoId-index',
    KeyConditionExpression: 'cgoId = :id',
    ExpressionAttributeValues: {
        ':id': { S: '0' }
    }
}

router.get('/listAll', (req: Request, res: Response) => {
    ddb.query(listAllArtisansParams, (err, data) => {
        if (err) {
            console.log('Error fetching artisans in artisan/listAll: ' + err)
            res.status(400).send(
                'Error fetching artisans in artisan/listAll: ' + err.message
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
        TableName: 'artisan',
        Item: {
            artisanId: { S: req.body.artisanId },
            cgoId: { S: req.body.cgoId },
            bio: { S: req.body.bio },
            city: { S: req.body.city },
            country: { S: req.body.country },
            name: { S: req.body.name },
            lat: { N: req.body.lat },
            lon: { N: req.body.lon },
            picURL: { S: 'Not set' }
        }
    }
    ddb.putItem(params, (err, data) => {
        if (err) {
            console.log('Error adding artisan in artisan/add: ', err)
            res.status(400).send(
                'Error adding artisan in artisan/add: ' + err.message
            )
        } else {
            res.send('Successfully added')
        }
    })
})

router.post('/updateImage', (req: Request, res: Response) => {
    // setup pic uploader with artisanId as filename
    const artisanPicsUploader = multer({
        storage: multerS3({
            s3,
            bucket: 'artisan-prof-pics-new',
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
                    req.body.artisanId + '.' + mime.getExtension(file.mimetype)
                )
            }
        })
    })

    const singleArtisanPicUpload = artisanPicsUploader.single('image')

    // upload pic
    singleArtisanPicUpload(req, res, picErr => {
        if (picErr) {
            console.log('Error uploading picture in artisan/update', picErr)
            res.status(422).send(
                'Error uploading picture in artisan/update: ' + picErr.message
            )
        } else {
            let picURL = 'Error: no picture attached'
            if (req.file) {
                picURL = (req.file as any).location
            }

            // update db record with new URL
            const params: aws.DynamoDB.UpdateItemInput = {
                TableName: 'artisan',
                Key: { artisanId: { S: req.body.artisanId } },
                UpdateExpression: 'set picURL = :u',
                ExpressionAttributeValues: { ':u': { S: picURL } },
                ReturnValues: 'UPDATED_NEW'
            }
            // check string, params
            ddb.updateItem(params, (err, data) => {
                if (err) {
                    console.log(
                        'Error updating artisan record ' +
                            req.body.artisanId +
                            ' : ' +
                            err
                    )
                    res.status(400).send(
                        'Error updating artisan record ' +
                            req.body.artisanId +
                            ' : ' +
                            err.message
                    )
                } else {
                    res.json({ imageUrl: picURL })
                }
            })
        }
    })
})

router.get('/deleteAll', (req: Request, res: Response) => {
    ddb.query(listAllArtisansParams, (err, data) => {
        if (err) {
            console.log(
                'Error getting all artisans in artisan/deleteAll: ' + err
            )
            res.status(400).send(
                'Error getting all artisans in artisan/deleteAll: ' +
                    err.message
            )
        } else {
            const convert = data.Items.map(item => {
                return new Promise(resolve => {
                    const unmarshed = aws.DynamoDB.Converter.unmarshall(item)
                    const params: aws.DynamoDB.DeleteItemInput = {
                        TableName: 'artisan',
                        Key: { artisanId: { S: unmarshed.artisanId } }
                    }
                    ddb.deleteItem(params, deleteErr => {
                        if (deleteErr) {
                            console.log(
                                'Error deleting an artisan in artisan/deleteAll: ' +
                                    deleteErr
                            )
                            res.status(402).send(
                                'Error deleting an artisan in artisan/deleteAll: ' +
                                    deleteErr.message
                            )
                        }
                        resolve('Deleted: ' + unmarshed.artisanId)
                    })
                })
            })
            Promise.all(convert).then(items => {
                res.send('All artisans have been deleted')
            })
        }
    })
})

export { router as artisanRouter }
