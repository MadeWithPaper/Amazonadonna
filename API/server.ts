import * as dotenv from 'dotenv'
dotenv.config()

import * as path from 'path'
import * as express from 'express'
import * as bodyParser from 'body-parser'
import * as aws from 'aws-sdk'
import { artisanRouter } from './routes/artisan'
import { orderRouter } from './routes/order'
import { itemRouter } from './routes/item'
import { cgaRouter } from './routes/cga'
import { payoutRouter } from './routes/payout'

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
})

app.use('/artisan', artisanRouter)
app.use('/order', orderRouter)
app.use('/item', itemRouter)
app.use('/cga', cgaRouter)
app.use('/payout', payoutRouter)

export { app as server, ddb, s3 }
