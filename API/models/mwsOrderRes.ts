interface MwsOrder {
    AmazonOrderId: string
    PurchaseDate: string
    LastUpdateDate: string
    OrderTotal: MwsMoney
    NumberOfItemsShipped: number
    NumberOfItemsUnshipped: number
    ShippingAddress: MwsAddress
}

interface MwsOrderRes {
    NextToken: string
    CreatedBefore: string
    Orders: MwsOrder[]
}

interface MwsMoney {
    CurrencyCode: string
    Amount: string
}

interface MwsAddress {
    Name: string
    AddressLine1: string
    AddressLine2: string
    AddressLine3: string
    City: string
    County: string
    District: string
    StateOrRegion: string
    PostalCode: string
    CountryCode: string
    Phone: string
    AddressType: string
}

export { MwsOrderRes, MwsMoney }
