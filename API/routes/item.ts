import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import * as multer from 'multer'
import * as multerS3 from 'multer-s3'
import * as mime from 'mime'
import { ddb, s3 } from '../server'
import { Item } from '../models/item'
import { unmarshUtil } from '../utilities/unmarshall'
import { filterActive } from '../utilities/active'
import * as uuid from 'uuid'
import * as _ from 'lodash'

const router = Router()

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
                res.json(filterActive(items))
            })
        }
    })
})

router.post('/add', (req: Request, res: Response) => {
    const id = uuid.v1()
    const putItemParams: aws.DynamoDB.PutItemInput = {
        TableName: 'item',
        Item: {
            itemId: { S: id },
            artisanId: { S: req.body.artisanId },
            price: { S: req.body.price },
            description: { S: req.body.description },
            category: { S: req.body.category },
            subCategory: { S: req.body.subCategory },
            specificCategory: { S: req.body.specificCategory },
            itemName: { S: req.body.itemName },
            shippingOption: { S: req.body.shippingOption },
            itemQuantity: { N: req.body.itemQuantity },
            productionTime: { N: req.body.productionTime },
            pic0URL: { S: 'undefined' },
            pic1URL: { S: 'undefined' },
            pic2URL: { S: 'undefined' },
            pic3URL: { S: 'undefined' },
            pic4URL: { S: 'undefined' },
            pic5URL: { S: 'undefined' },
            active: { S: 'true' }
        }
    }
    ddb.putItem(putItemParams, (err, data) => {
        if (err) {
            console.log('Error adding item in item/add: ', err)
            res.status(400).send(
                'Error adding item in item/add: ' + err.message
            )
        } else {
            res.json(id.toString())
        }
    })
})

router.post('/updateImage', (req: Request, res: Response) => {
    // setup pic uploader with itemId as filename
    const artisanPicsUploader = multer({
        storage: multerS3({
            s3,
            bucket: 'artisan-item-pics',
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
                    req.body.itemId +
                        '-' +
                        Date.now() +
                        '.' +
                        mime.getExtension(file.mimetype)
                )
            }
        })
    })

    const singleItemPicUpload = artisanPicsUploader.single('image')

    // upload pic
    singleItemPicUpload(req, res, picErr => {
        const picNum = Number(req.body.picIndex)
        if (picErr) {
            console.log('Error uploading picture in item/updateImage', picErr)
            res.status(422).send(
                'Error uploading picture in item/updateImage: ' + picErr.message
            )
        } else if (isNaN(picNum) || picNum < 0 || picNum >= 6) {
            const msg = 'Error updating item ' + req.body.itemId + ' with pic: '
            console.log(msg + 'picIndex must be 0-5')
            res.status(400).send(msg + 'picIndex must be 0-5')
        } else {
            let picURL = 'Error: no picture attached'
            if (req.file) {
                picURL = (req.file as any).location
            }
            const updateExpress = 'set pic' + req.body.picIndex + 'URL = :u'
            const params: aws.DynamoDB.UpdateItemInput = {
                TableName: 'item',
                Key: { itemId: { S: req.body.itemId } },
                UpdateExpression: updateExpress,
                ExpressionAttributeValues: { ':u': { S: picURL } },
                ReturnValues: 'UPDATED_NEW',
                ConditionExpression: 'attribute_exists(itemId)'
            }
            // check string, params
            ddb.updateItem(params, (err, data) => {
                if (err) {
                    const msg =
                        'Error updating item ' + req.body.itemId + ' with pic: '
                    console.log(msg + err)
                    res.status(400).send(msg + err.message)
                } else {
                    res.json(picURL)
                }
            })
        }
    })
})

router.post('/editItem', (req: Request, res: Response) => {
    // Get current item data
    const getItemParams: aws.DynamoDB.Types.GetItemInput = {
        TableName: 'item',
        Key: { itemId: { S: req.body.itemId } }
    }
    ddb.getItem(getItemParams, (err, data) => {
        if (err) {
            console.log('Error getting item in item/editItem/getItem: ' + err)
            res.status(400).send(
                'Error getting item in item/editItem/getItem: ' + err.message
            )
        } else {
            const unmarshed = aws.DynamoDB.Converter.unmarshall(data.Item)
            if (_.isEmpty(unmarshed)) {
                const msg = 'Error getting item in item/editItem/getItem: '
                const DNEerr = 'Item does not exist'
                console.log(msg + DNEerr)
                res.status(400).send(msg + DNEerr)
            } else {
                const whatToUpdate: Item = {
                    itemId: unmarshed.itemId,
                    artisanId: req.body.artisanId
                        ? req.body.artisanId
                        : unmarshed.artisanId,
                    price: req.body.price ? req.body.price : unmarshed.price,
                    description: req.body.description
                        ? req.body.description
                        : unmarshed.description,
                    category: req.body.category
                        ? req.body.category
                        : unmarshed.category,
                    subCategory: req.body.subCategory
                        ? req.body.subCategory
                        : unmarshed.subCategory,
                    specificCategory: req.body.specificCategory
                        ? req.body.specificCategory
                        : unmarshed.specificCategory,
                    itemName: req.body.itemName
                        ? req.body.itemName
                        : unmarshed.itemName,
                    shippingOption: req.body.shippingOption
                        ? req.body.shippingOption
                        : unmarshed.shippingOption,
                    itemQuantity: req.body.itemQuantity
                        ? req.body.itemQuantity
                        : unmarshed.itemQuantity,
                    productionTime: req.body.productionTime
                        ? req.body.productionTime
                        : unmarshed.productionTime,
                    pic0URL: req.body.pic0URL
                        ? req.body.pic0URL
                        : unmarshed.pic0URL,
                    pic1URL: req.body.pic1URL
                        ? req.body.pic1URL
                        : unmarshed.pic1URL,
                    pic2URL: req.body.pic2URL
                        ? req.body.pic2URL
                        : unmarshed.pic2URL,
                    pic3URL: req.body.pic3URL
                        ? req.body.pic3URL
                        : unmarshed.pic3URL,
                    pic4URL: req.body.pic4URL
                        ? req.body.pic4URL
                        : unmarshed.pic4URL,
                    pic5URL: req.body.pic5URL
                        ? req.body.pic5URL
                        : unmarshed.pic5URL
                }

                const editItemParams: aws.DynamoDB.Types.UpdateItemInput = {
                    TableName: 'item',
                    Key: { itemId: { S: req.body.itemId } },
                    UpdateExpression: `set artisanId = :artisanId, 
                                    price = :price, 
                                    description = :description,
                                    category = :category,
                                    subCategory = :subCategory,
                                    specificCategory = :specificCategory,
                                    itemName = :itemName,
                                    shippingOption = :shippingOption,
                                    itemQuantity = :itemQuantity,
                                    productionTime = :productionTime,
                                    pic0URL = :pic0URL,
                                    pic1URL = :pic1URL,
                                    pic2URL = :pic2URL,
                                    pic3URL = :pic3URL,
                                    pic4URL = :pic4URL,
                                    pic5URL = :pic5URL`,
                    ExpressionAttributeValues: {
                        ':artisanId': { S: whatToUpdate.artisanId },
                        ':price': { S: whatToUpdate.price },
                        ':description': { S: whatToUpdate.description },
                        ':category': { S: whatToUpdate.category },
                        ':subCategory': { S: whatToUpdate.subCategory },
                        ':specificCategory': {
                            S: whatToUpdate.specificCategory
                        },
                        ':itemName': { S: whatToUpdate.itemName },
                        ':shippingOption': { S: whatToUpdate.shippingOption },
                        ':itemQuantity': {
                            N: whatToUpdate.itemQuantity.toString()
                        },
                        ':productionTime': {
                            N: whatToUpdate.productionTime.toString()
                        },
                        ':pic0URL': { S: whatToUpdate.pic0URL },
                        ':pic1URL': { S: whatToUpdate.pic1URL },
                        ':pic2URL': { S: whatToUpdate.pic2URL },
                        ':pic3URL': { S: whatToUpdate.pic3URL },
                        ':pic4URL': { S: whatToUpdate.pic4URL },
                        ':pic5URL': { S: whatToUpdate.pic5URL }
                    },
                    ReturnValues: 'UPDATED_NEW'
                }
                ddb.updateItem(editItemParams, (updateErr, updateData) => {
                    if (updateErr) {
                        console.log(
                            'Error updating item in item/editItem: ' + updateErr
                        )
                        res.status(400).send(
                            'Error updating item in item/editItem: ' +
                                updateErr.message
                        )
                    } else {
                        res.send('Success!')
                    }
                })
            }
        }
    })
})

router.post('/delete', (req: Request, res: Response) => {
    const getItemParams: aws.DynamoDB.Types.GetItemInput = {
        TableName: 'item',
        Key: { itemId: { S: req.body.itemId } }
    }
    ddb.getItem(getItemParams, (err, data) => {
        if (err) {
            console.log('Error getting item in item/delete/getItem: ' + err)
            res.status(400).send(
                'Error getting item in item/delete/getItem: ' + err.message
            )
        } else {
            const unmarshed = aws.DynamoDB.Converter.unmarshall(data.Item)
            if (_.isEmpty(unmarshed)) {
                const msg = 'Error getting item in item/delete/getItem: '
                const DNEerr = 'Item does not exist'
                console.log(msg + DNEerr)
                res.status(400).send(msg + DNEerr)
            } else {
                const editItemParams: aws.DynamoDB.Types.UpdateItemInput = {
                    TableName: 'item',
                    Key: { itemId: { S: req.body.itemId } },
                    UpdateExpression: `set active = :active`,
                    ExpressionAttributeValues: {
                        ':active': { S: 'false' }
                    },
                    ReturnValues: 'UPDATED_NEW'
                }
                ddb.updateItem(editItemParams, (updateErr, updateData) => {
                    if (updateErr) {
                        console.log(
                            'Error updating item in item/delete: ' + updateErr
                        )
                        res.status(400).send(
                            'Error updating item in item/delete: ' +
                                updateErr.message
                        )
                    } else {
                        res.send('Success!')
                    }
                })
            }
        }
    })
})

export { router as itemRouter }
