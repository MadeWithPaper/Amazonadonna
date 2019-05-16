import * as _ from 'lodash'

const filterActive = (items: any) => {
    if (items.length && items.length > 1) {
        return _.filter(items, (o: any) => {
            return o.active === undefined || o.active === 'true'
        })
    } else {
        if (items.active === undefined || items.active === 'true') {
            return items
        } else {
            return {}
        }
    }
}

export { filterActive }
