import * as chai from 'chai'
import chaiHttp = require('chai-http')
import { server, ddb } from '../server'
import * as aws from 'aws-sdk'

chai.use(chaiHttp)
const should = chai.should()

describe('artisans', () => {
    before(done => {
        ddb.listTables((err, data) => {
            if (err) {
                console.log('Error listing tables: ' + err)
            } else {
                const deleteTables = data.TableNames.map(table => {
                    return new Promise(resolve => {
                        const deleteInput: aws.DynamoDB.DeleteTableInput = {
                            TableName: table
                        }
                        ddb.deleteTable(
                            deleteInput,
                            (deleteErr, deleteData) => {
                                if (deleteErr) {
                                    console.log(
                                        'Error dropping table: ' + table
                                    )
                                }
                                resolve(deleteData)
                            }
                        )
                    })
                })
                Promise.all(deleteTables).then(() => {
                    const addArtisanTable: aws.DynamoDB.CreateTableInput = {
                        TableName: 'artisan',
                        AttributeDefinitions: [
                            { AttributeName: 'artisanId', AttributeType: 'S' },
                            { AttributeName: 'cgoId', AttributeType: 'S' },
                            { AttributeName: 'bio', AttributeType: 'S' },
                            { AttributeName: 'city', AttributeType: 'S' },
                            { AttributeName: 'country', AttributeType: 'S' },
                            { AttributeName: 'name', AttributeType: 'S' },
                            { AttributeName: 'lat', AttributeType: 'N' },
                            { AttributeName: 'lon', AttributeType: 'N' },
                            { AttributeName: 'picURL', AttributeType: 'S' }
                        ],
                        KeySchema: [
                            { AttributeName: 'artisanId', KeyType: 'S' }
                        ],
                        ProvisionedThroughput: {
                            ReadCapacityUnits: 40000,
                            WriteCapacityUnits: 40000
                        }
                    }
                    ddb.createTable(addArtisanTable, addTableErr => {
                        if (err) {
                            console.log('Error creating table: ' + addTableErr)
                        }
                        ddb.listTables((testerr, testdata) => {
                            console.log(testdata)
                            done()
                        })
                    })
                })
            }
        })
    })

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
