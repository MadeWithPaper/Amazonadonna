interface Payout {
    payoutId: string
    artisanId: string
    cgoId: string
    amount: number
    date: Date
    signaturePicURL: string
}

export { Payout }
