import { MwsMoney } from './mwsOrderRes'

interface MwsOrderItemRes {
    NextToken: string
    AmazonOrderId: string
    OrderItems: MwsOrderItem[]
}

interface MwsOrderItem {
    ASIN: string
    OrderItemId: string
    Title: string
    QuantityOrdered: number
    ItemPrice: MwsMoney
}

export { MwsOrderItemRes }
