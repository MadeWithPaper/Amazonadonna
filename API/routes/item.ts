import { Router, Request, Response } from 'express'
import * as aws from 'aws-sdk'
import * as multer from 'multer'
import * as multerS3 from 'multer-s3'
import * as mime from 'mime'
import { ddb, s3 } from '../server'

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
    const putItemParams: aws.DynamoDB.PutItemInput = {
        TableName: 'item',
        Item: {
            itemId: { N: req.body.itemId },
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
            picURLs: {
                L: [
                    { S: 'Not set' },
                    { S: 'Not set' },
                    { S: 'Not set' },
                    { S: 'Not set' },
                    { S: 'Not set' },
                    { S: 'Not set' }
                ]
            }
        }
    }
    ddb.putItem(putItemParams, (err, data) => {
        if (err) {
            console.log('Error adding artisan in item/add: ', err)
            res.status(400).send(
                'Error adding artisan in item/add: ' + err.message
            )
        } else {
            res.send('Successfully added')
        }
    })
})

router.post('/updateImages', (req: Request, res: Response) => {
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
                    req.body.itemId + '.' + mime.getExtension(file.mimetype)
                )
            }
        })
    })

    const mulitItemPicUpload = artisanPicsUploader.array('image', 6)

    // upload pic
    mulitItemPicUpload(req, res, picErr => {
        if (picErr) {
            console.log('Error uploading picture in item/updateImage', picErr)
            res.status(422).send(
                'Error uploading picture in item/updateImage: ' + picErr.message
            )
        } else {
            let picURLs: aws.DynamoDB.AttributeValue[] = [
                { S: 'Not set' },
                { S: 'Not set' },
                { S: 'Not set' },
                { S: 'Not set' },
                { S: 'Not set' },
                { S: 'Not set' }
            ]
            if (req.files) {
                const getURLs = (req.files as any[]).map(file => {
                    return new Promise<aws.DynamoDB.AttributeValue>(resolve => {
                        resolve({ S: (file as any).location })
                    })
                })
                Promise.all(getURLs).then(urls => {
                    picURLs = urls
                })
            }

            // update db record with new URL
            const params: aws.DynamoDB.UpdateItemInput = {
                TableName: 'item',
                Key: { itemId: { N: req.body.itemId } },
                UpdateExpression: 'set picURLs = :u',
                ExpressionAttributeValues: { ':u': { L: picURLs } },
                ReturnValues: 'UPDATED_NEW'
            }
            // check string, params
            ddb.updateItem(params, (err, data) => {
                if (err) {
                    console.log(
                        'Error updating item record with pic ' +
                            req.body.itemId +
                            ' : ' +
                            err
                    )
                    res.status(400).send(
                        'Error updating item record with pic ' +
                            req.body.itemId +
                            ' : ' +
                            err.message
                    )
                } else {
                    res.send('urls updated!')
                }
            })
        }
    })
})

router.post('/editItem', (req: Request, res: Response) => {
    const editItemParams: aws.DynamoDB.Types.UpdateItemInput = {
        TableName: 'item',
        Key: { itemId: { N: req.body.itemId } },
        UpdateExpression: `set artisanId = :artisanId, 
                           set price = :price, 
                           set description = :desciption,
                           set category = :category,
                           set subCategory = :subCategory,
                           set specificCategory = :specificCategory,
                           set itemName = :itemName,
                           set shippingOption = :shippingOption,
                           set itemQuantity = :itemQuantity,
                           set productionTime = :productionTime,
                           set picURLs = :picURLs`,
        ExpressionAttributeValues: {
            ':artisanId': { S: req.body.artisanId },
            ':price': { S: req.body.price },
            ':description': { S: req.body.description },
            ':category': { S: req.body.category },
            ':subCategory': { S: req.body.subCategory },
            ':specificCategory': { S: req.body.specificCategory },
            ':itemName': { S: req.body.itemName },
            ':shippingOption': { S: req.body.shippingOption },
            ':itemQuantity': { N: req.body.itemQuantity },
            ':productionTime': { N: req.body.productionTime },
            ':picURLs': {
                L: [
                    { S: 'Not set' },
                    { S: 'Not set' },
                    { S: 'Not set' },
                    { S: 'Not set' },
                    { S: 'Not set' },
                    { S: 'Not set' }
                ]
            }
        },
        ReturnValues: 'UPDATED_NEW'
    }
    ddb.updateItem(editItemParams, (err, data) => {
        if (err) {
            console.log(
                'Error updating shipped status in item/editItem: ' + err
            )
            res.status(400).send(
                'Error updating shipped status in item/editItem: ' + err.message
            )
        } else {
            res.send('Success!')
        }
    })
})

router.post('/delete', (req: Request, res: Response) => {
    const deleteItemParams: aws.DynamoDB.Types.DeleteItemInput = {
        TableName: 'item',
        Key: { itemId: { N: req.body.itemId } }
    }

    ddb.deleteItem(deleteItemParams, (err, data) => {
        if (err) {
            console.log('Error fetching items in item/delete: ' + err)
            res.status(400).send(
                'Error fetching artisans in item/delete: ' + err.message
            )
        } else {
            res.send('Success!')
        }
    })
})

export { router as itemRouter }
