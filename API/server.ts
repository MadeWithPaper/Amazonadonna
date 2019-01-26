import * as dotenv from 'dotenv'
dotenv.config()

import * as path from 'path'
import * as express from 'express'
import * as bodyParser from 'body-parser'
import * as aws from 'aws-sdk'
import * as multer from 'multer'
import * as multerS3 from 'multer-s3'
import * as mime from 'mime'

const app = express()
const port = process.env.PORT || 3000
const dev = process.env.PROD === 'false'
const test = process.env.NODE_ENV === 'test'

aws.config.update({ region: 'us-east-1' })
let ddb = new aws.DynamoDB({ apiVersion: '2012-10-08' })

if (test) {
    ddb = new aws.DynamoDB({
        apiVersion: '2012-10-08',
        endpoint: 'http://localhost:8000'
    })
}

const s3 = new aws.S3()

app.use(bodyParser.urlencoded({ extended: true }))
app.use(bodyParser.json())

app.listen(port, () => {
    console.log(`App is listening on port ${port}`)
})

app.get('/', (req: express.Request, res: express.Response) => {
    ddb.listTables({ Limit: 10 }, (err, data) => {
        console.log(data)
    })
    res.send('I did it!')
    ddb.listTables({ Limit: 10 }, (err, data) => {
        if (err) {
            console.log('Error', err.code)
        } else {
            console.log('Table names are ', data.TableNames)
        }
    })
    // res.sendFile(path.resolve(__dirname, 'frontEnd', 'index.html'))
})

const listAllArtisansParams: aws.DynamoDB.Types.QueryInput = {
    TableName: 'artisan',
    IndexName: 'cgoId-index',
    KeyConditionExpression: 'cgoId = :id',
    ExpressionAttributeValues: {
        ':id': { S: '0' }
    }
}

app.get('/artisans', (req: express.Request, res: express.Response) => {
    ddb.query(listAllArtisansParams, (err, data) => {
        if (err) {
            console.log('Error fetching artisans in artisans: ' + err)
            res.status(400).send(
                'Error fetching artisans in artisans: ' + err.message
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

app.post(
    '/addArtisanToDatabase',
    (req: express.Request, res: express.Response) => {
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
                console.log(
                    'Error adding artisan in addArtisanToDatabase: ',
                    err
                )
                res.status(400).send(
                    'Error adding artisan in addArtisanToDatabase: ' +
                        err.message
                )
            } else {
                res.send('Successfully added')
            }
        })
    }
)

app.post(
    '/updateArtisanImage',
    (req: express.Request, res: express.Response) => {
        // setup pic uploader with artisanId as filename
        const artisanPicsUploader = multer({
            storage: multerS3({
                s3,
                bucket: 'artisan-prof-pics',
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
                        req.body.artisanId +
                            '.' +
                            mime.getExtension(file.mimetype)
                    )
                }
            })
        })

        const singleArtisanPicUpload = artisanPicsUploader.single('image')

        // upload pic
        singleArtisanPicUpload(req, res, picErr => {
            if (picErr) {
                console.log(
                    'Error uploading picture in updateArtisanImage',
                    picErr
                )
                res.status(422).send(
                    'Error uploading picture in updateArtisanImage: ' +
                        picErr.message
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
    }
)

app.get('/deleteAllArtisans', (req: express.Request, res: express.Response) => {
    ddb.query(listAllArtisansParams, (err, data) => {
        if (err) {
            console.log(
                'Error getting all artisans in deleteAllArtisans: ' + err
            )
            res.status(400).send(
                'Error getting all artisans in deleteAllArtisans: ' +
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
                                'Error in deleting an artisan in deleteAllArtisans: ' +
                                    deleteErr
                            )
                            res.status(402).send(
                                'Error in deleting an artisan in deleteAllArtisans: ' +
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

export { app as server, ddb }

// app.use(express.static(path.resolve(__dirname, 'frontEnd')))
