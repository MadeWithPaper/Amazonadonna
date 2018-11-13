import * as dotenv from 'dotenv'
dotenv.config()

import * as path from 'path'
import * as express from 'express'

const app = express()
const port = process.env.PORT || 3000
const dev = process.env.PROD === 'false'

app.listen(port, () => {
    console.log(`App is listening on port ${port}`)
})

app.get('/', (req: express.Request, res: express.Response) => {
    res.send('we did it!')
    // res.sendFile(path.resolve(__dirname, 'frontEnd', 'index.html'))
})

// app.use(express.static(path.resolve(__dirname, 'frontEnd')))
