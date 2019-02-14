import * as aws from 'aws-sdk'
interface UnmarshedData {
    [key: string]: any
}

const unmarshUtil = (
    items: aws.DynamoDB.AttributeMap[]
): Array<Promise<UnmarshedData>> => {
    const convert = items.map(item => {
        return new Promise(resolve => {
            const unmarshed = aws.DynamoDB.Converter.unmarshall(item)
            resolve(unmarshed)
        })
    })
    return convert
}

export { unmarshUtil }
