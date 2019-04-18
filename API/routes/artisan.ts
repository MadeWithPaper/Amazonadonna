import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import * as multer from 'multer'
import * as multerS3 from 'multer-s3'
import * as mime from 'mime'
import { ddb, s3 } from '../server'
import { unmarshUtil } from '../utilities/unmarshall'
import { Artisan } from '../models/artisan'
import * as uuid from 'uuid'

const router = Router()

router.post('/listAllForCga', (req: Request, res: Response) => {
    const listAllArtisansParams: aws.DynamoDB.Types.QueryInput = {
        TableName: 'artisan',
        IndexName: 'cgaId-index',
        KeyConditionExpression: 'cgaId = :id',
        ExpressionAttributeValues: {
            ':id': { S: req.body.cgaId }
        }
    }

    ddb.query(listAllArtisansParams, (err, data) => {
        if (err) {
            const msg = 'Error fetching artisans in artisan/listAllForCga: '
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
    const putItemParams: aws.DynamoDB.PutItemInput = {
        TableName: 'artisan',
        Item: {
            artisanId: { S: id },
            cgaId: { S: req.body.cgaId },
            bio: { S: req.body.bio },
            city: { S: req.body.city },
            country: { S: req.body.country },
            artisanName: { S: req.body.artisanName },
            lat: { N: req.body.lat },
            lon: { N: req.body.lon },
            balance: { N: req.body.balance },
            picURL: { S: 'Not set' },
            phoneNumber: { S: req.body.phoneNumber }
        }
    }
    ddb.putItem(putItemParams, (err, data) => {
        if (err) {
            const msg = 'Error adding artisan in artisan/add: '
            console.log(msg, err)
            res.status(400).send(msg + err.message)
        } else {
            res.json(id.toString())
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
            const msg = 'Error uploading picture in artisan/update'
            console.log(msg, picErr)
            res.status(422).send(msg + picErr.message)
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
                    const msg = 'Error updating artisan record '
                    console.log(msg + req.body.artisanId + ' : ' + err)
                    res.status(400).send(
                        msg + req.body.artisanId + ' : ' + err.message
                    )
                } else {
                    res.json({ imageUrl: picURL })
                }
            })
        }
    })
})

router.post('/getById', (req: Request, res: Response) => {
    const getArtisanParams: aws.DynamoDB.Types.GetItemInput = {
        TableName: 'artisan',
        Key: { artisanId: { S: req.body.artisanId } }
    }
    ddb.getItem(getArtisanParams, (err, data) => {
        if (err) {
            const msg = 'Error getting all artisans in artisan/getById: '
            console.log(msg + err)
            res.status(400).send(msg + err.message)
        } else {
            res.json(aws.DynamoDB.Converter.unmarshall(data.Item))
        }
    })
})

router.post('/edit', (req: Request, res: Response) => {
    // Get current artisan data
    const getArtisanParams: aws.DynamoDB.Types.GetItemInput = {
        TableName: 'artisan',
        Key: { artisanId: { S: req.body.artisanId } }
    }
    ddb.getItem(getArtisanParams, (err, data) => {
        if (err) {
            const msg = 'Error getting artisan in artisan/edit/getArtisan: '
            console.log(msg + err)
            res.status(400).send(msg + err.message)
        } else {
            console.log('found record for: ' + req.body.artisanId)
            const unmarshed = aws.DynamoDB.Converter.unmarshall(data.Item)
            console.log(unmarshed.artisanId)
            const whatToUpdate: Artisan = {
                artisanId: unmarshed.artisanId,
                cgaId: req.body.cgaId ? req.body.cgaId : unmarshed.cgaId,
                bio: req.body.bio ? req.body.bio : unmarshed.bio,
                city: req.body.city ? req.body.city : unmarshed.city,
                country: req.body.country
                    ? req.body.country
                    : unmarshed.country,
                artisanName: req.body.artisanName
                    ? req.body.artisanName
                    : unmarshed.artisanName,
                lat: req.body.lat ? req.body.lat : unmarshed.lat,
                lon: req.body.lon ? req.body.lon : unmarshed.lon,
                balance: req.body.balance
                    ? req.body.balance
                    : unmarshed.balance,
                picURL: req.body.picURL ? req.body.picURL : unmarshed.picURL,
                phoneNumber: req.body.phoneNumber
                    ? req.body.phoneNumber
                    : unmarshed.phoneNumber
            }

            const editArtisanParam: aws.DynamoDB.Types.UpdateItemInput = {
                TableName: 'artisan',
                Key: { artisanId: { S: req.body.artisanId } },
                UpdateExpression: `set cgaId = :cgaId, 
                                    bio = :bio, 
                                    city = :city,
                                    country = :country,
                                    artisanName = :artisanName,
                                    lat = :lat,
                                    lon = :lon,
                                    balance = :balance,
                                    picURL = :picURL`,
                ExpressionAttributeValues: {
                    ':cgaId': { S: whatToUpdate.cgaId },
                    ':bio': { S: whatToUpdate.bio },
                    ':city': { S: whatToUpdate.city },
                    ':country': { S: whatToUpdate.country },
                    ':artisanName': { S: whatToUpdate.artisanName },
                    ':lat': { N: whatToUpdate.lat.toString() },
                    ':lon': { N: whatToUpdate.lon.toString() },
                    ':balance': { N: whatToUpdate.balance.toString() },
                    ':picURL': { S: whatToUpdate.picURL }
                },
                ReturnValues: 'UPDATED_NEW'
            }
            ddb.updateItem(editArtisanParam, (updateErr, updateData) => {
                if (updateErr) {
                    const msg = 'Error updating artisan in artisan/edit: '
                    console.log(msg + updateErr)
                    res.status(400).send(msg + updateErr.message)
                } else {
                    res.send('Success!')
                }
            })
        }
    })
})

router.get('/deleteAll', (req: Request, res: Response) => {
    const listAllArtisansParams: aws.DynamoDB.Types.QueryInput = {
        TableName: 'artisan',
        IndexName: 'cgaId-index',
        KeyConditionExpression: 'cgaId = :id',
        ExpressionAttributeValues: {
            ':id': { S: '0' }
        }
    }

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
