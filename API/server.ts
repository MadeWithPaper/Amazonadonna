import * as dotenv from 'dotenv'
dotenv.config()

// Look into amazon sdk

import * as path from 'path'
import * as express from 'express'
import * as bodyParser from 'body-parser'
import * as aws from 'aws-sdk'

const app = express()
const port = process.env.PORT || 3000
const dev = process.env.PROD === 'false'

aws.config.update({ region: 'us-east-1' })
const ddb = new aws.DynamoDB({ apiVersion: '2012-10-08' })

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
            console.log(err)
            res.sendStatus(400)
        } else {
            res.json(data.Items)
        }
    })
})

app.get('/getDatabase', (req: express.Request, res: express.Response) => {
    res.json({ name: 'Mitchell' })
})

app.post('/addToDatabase', (req: express.Request, res: express.Response) => {
    console.log(req.body)
    res.send()
})

// app.use(express.static(path.resolve(__dirname, 'frontEnd')))
