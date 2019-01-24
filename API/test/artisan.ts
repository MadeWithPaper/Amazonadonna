import * as chai from 'chai'
import chaiHttp = require('chai-http')
import { server } from '../server'

chai.use(chaiHttp)
const should = chai.should()

describe('artisans', () => {
    describe('listAllArtisans', () => {
        it('it should GET all the books', done => {
            chai.request(server)
                .get('/artisans')
                .end((err, res) => {
                    res.should.have.status(200)
                    res.body.should.be.an('Object')
                    done()
                })
        })
    })
})
