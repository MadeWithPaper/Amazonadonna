import * as chai from 'chai'
import chaiHttp = require('chai-http')
import { server } from '../server'

const should = chai.should()
chai.use(chaiHttp)

describe('artisans', () => {
    describe('listAllArtisans', () => {
        it('it should GET all the books', done => {
            chai.request(server)
                .get('/')
                .end((err, res) => {
                    done()
                })
        })
    })
})
