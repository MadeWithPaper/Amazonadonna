import * as _ from 'lodash'

const filterActive = (items: any) => {
    return _.filter(items, (o: any) => {
        return o.active === undefined || o.active === 'true'
    })
}

export { filterActive }
