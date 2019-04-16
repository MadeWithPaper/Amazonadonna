import * as chai from 'chai'
import chaiHttp = require('chai-http')
import { server, ddb } from '../server'
import * as aws from 'aws-sdk'

chai.use(chaiHttp)
const should = chai.should()

describe('artisans', () => {
    before(done => {
        // drop all existing tables
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
                            { AttributeName: 'cgaId', AttributeType: 'S' }
                        ],
                        KeySchema: [
                            { AttributeName: 'artisanId', KeyType: 'HASH' }
                        ],
                        ProvisionedThroughput: {
                            ReadCapacityUnits: 5,
                            WriteCapacityUnits: 5
                        },
                        GlobalSecondaryIndexes: [
                            {
                                IndexName: 'cgaId-index',
                                KeySchema: [
                                    {
                                        AttributeName: 'cgaId',
                                        KeyType: 'HASH'
                                    }
                                ],
                                Projection: {
                                    ProjectionType: 'ALL'
                                },
                                ProvisionedThroughput: {
                                    ReadCapacityUnits: 1,
                                    WriteCapacityUnits: 1
                                }
                            }
                        ]
                    }
                    // create artisan table
                    ddb.createTable(addArtisanTable, addTableErr => {
                        if (addTableErr) {
                            console.log('Error creating table: ' + addTableErr)
                        }
                        done()
                    })
                })
            }
        })
    })

    describe('listAllArtisans', () => {
        it('it should GET all the artisans (empty)', done => {
            chai.request(server)
                .get('/artisan/listAll')
                .end((err, res) => {
                    res.should.have.status(200)
                    res.body.should.be.an('Array')
                    res.body.length.should.be.eql(0)
                    done()
                })
        })

        describe('listAllArtisans with artisans', () => {
            before(done => {
                const params: aws.DynamoDB.PutItemInput = {
                    TableName: 'artisan',
                    Item: {
                        artisanId: { S: '1234' },
                        cgaId: { S: '0' },
                        bio: { S: 'test bio!' },
                        city: { S: 'SLO, CA' },
                        country: { S: 'USA' },
                        name: { S: 'Cory' },
                        lat: { N: '44' },
                        lon: { N: '22' },
                        picURL: { S: 'Not set' }
                    }
                }
                ddb.putItem(params, (err, data) => {
                    if (err) {
                        console.log('Error adding test artisan: ' + err)
                    }
                    done()
                })
            })
            it('it should GET all the artisans', done => {
                chai.request(server)
                    .get('/artisan/listAll')
                    .end((err, res) => {
                        res.should.have.status(200)
                        res.body.should.be.an('Array')
                        res.body.length.should.be.eql(1)
                        res.body[0].should.be.an('Object')
                        res.body[0].should.be.eql({
                            artisanId: '1234',
                            cgaId: '0',
                            bio: 'test bio!',
                            city: 'SLO, CA',
                            country: 'USA',
                            name: 'Cory',
                            lat: 44,
                            lon: 22,
                            picURL: 'Not set'
                        })
                        done()
                    })
            })
        })
    })

    describe('addArtisanToDatabase', () => {
        it('it should POST an artisan', done => {
            chai.request(server)
                .post('/artisan/add')
                .send({
                    artisanId: '5678',
                    cgaId: '0',
                    bio: 'testing adding artisan',
                    city: 'Tampa',
                    country: 'USA',
                    name: 'Jackson',
                    lat: '32.5149',
                    lon: '117.0382'
                })
                .end((err, res) => {
                    res.should.have.status(200)
                    done()
                })
        })
        it('it should GET the new artisan', done => {
            chai.request(server)
                .get('/artisan/listAll')
                .end((err, res) => {
                    res.should.have.status(200)
                    res.body.should.be.an('Array')
                    res.body.length.should.be.eql(2)
                    res.body[1].should.be.an('Object')
                    res.body[1].should.be.eql({
                        artisanId: '5678',
                        cgaId: '0',
                        bio: 'testing adding artisan',
                        city: 'Tampa',
                        country: 'USA',
                        name: 'Jackson',
                        lat: 32.5149,
                        lon: 117.0382,
                        picURL: 'Not set'
                    })
                    done()
                })
        })
    })
})
