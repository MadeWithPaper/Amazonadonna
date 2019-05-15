import * as _ from 'lodash'

const filterActive = (items: any) => {
    return _.filter(items, { active: 'true' })
}

export { filterActive }
