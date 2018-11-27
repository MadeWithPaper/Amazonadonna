import * as dotenv from 'dotenv'
dotenv.config()

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

app.get('/getDatabase', (req: express.Request, res: express.Response) => {
    res.json({ name: 'Mitchell' })
})

const params: aws.DynamoDB.PutItemInput = {
    TableName: 'artisan',
    Item: {
        artisanId: { S: '1928485' },
        cgoId: { S: '90218495' },
        bio: { S: 'Hi I am artisan' },
        city: { S: 'Tijuana' },
        country: { S: 'MX' },
        name: { S: 'Jose Calderon' },
        lat: { N: '32.5149' },
        long: { N: '117.0382' }
    }
}
app.post(
    '/addArtisanToDatabase',
    (req: express.Request, res: express.Response) => {
        console.log(req.body)
        res.send()
        ddb.putItem(params, (err, data) => {
            if (err) {
                console.log('Error', err.code)
            } else {
                console.log('Attributes ', data)
            }
        })
    }
)

// app.use(express.static(path.resolve(__dirname, 'frontEnd')))
