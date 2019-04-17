interface Payout {
    payoutId: string
    artisanId: string
    cgaId: string
    amount: number
    date: Date
    signaturePicURL: string
}

export { Payout }
