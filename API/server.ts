import * as dotenv from 'dotenv'
dotenv.config()

import * as path from 'path'
import * as express from 'express'
import * as bodyParser from 'body-parser'

const app = express()
const port = process.env.PORT || 3000
const dev = process.env.PROD === 'false'

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
