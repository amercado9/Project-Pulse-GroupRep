import moment from 'moment'

export const getCurrentWeekNumber = () => moment().format('YYYY-[W]WW')

export const getCurrentWeekRange = () =>
  moment().startOf('isoWeek').format('MM-DD-YYYY') +
  ' -- ' +
  moment().endOf('isoWeek').format('MM-DD-YYYY')

export const getPreviousWeekRange = () =>
  moment().subtract(1, 'weeks').startOf('isoWeek').format('MM-DD-YYYY') +
  ' -- ' +
  moment().subtract(1, 'weeks').endOf('isoWeek').format('MM-DD-YYYY')

export const getPreviousWeek = () => moment().subtract(1, 'weeks').format('YYYY-[W]WW')
