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

aws.config.update({region: 'us-west-2'})
const ddb = new aws.DynamoDB({apiVersion: '2012-10-08'})

app.use(bodyParser.urlencoded({ extended: true }))
app.use(bodyParser.json())

app.listen(port, () => {
    console.log(`App is listening on port ${port}`)
})

app.get('/', (req: express.Request, res: express.Response) => {
    res.send('I did it!')
    // res.sendFile(path.resolve(__dirname, 'frontEnd', 'index.html'))
})

app.get('/getDatabase', (req: express.Request, res: express.Response) => {
    res.json({ name: 'Mitchell' })
})

app.post('/addToDatabase', (req: express.Request, res: express.Response) => {
    console.log(req.body)
    res.send()
})

// app.use(express.static(path.resolve(__dirname, 'frontEnd')))
