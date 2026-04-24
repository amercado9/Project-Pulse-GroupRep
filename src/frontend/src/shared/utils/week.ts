import moment from 'moment'

export const getCurrentWeekNumber = () => moment().format('YYYY-[W]WW')

export const formatIsoWeekLabel = (isoWeek: string) => {
  const [year, week] = isoWeek.split('-W') as [string, string]
  return `Week ${parseInt(week, 10)} (${year})`
}

export const getCurrentWeekRange = () =>
  moment().startOf('isoWeek').format('MM-DD-YYYY') +
  ' -- ' +
  moment().endOf('isoWeek').format('MM-DD-YYYY')

export const getPreviousWeekRange = () =>
  moment().subtract(1, 'weeks').startOf('isoWeek').format('MM-DD-YYYY') +
  ' -- ' +
  moment().subtract(1, 'weeks').endOf('isoWeek').format('MM-DD-YYYY')

export const getPreviousWeek = () => moment().subtract(1, 'weeks').format('YYYY-[W]WW')

export const isFutureIsoWeek = (isoWeek: string) =>
  moment(isoWeek + '-1', 'GGGG-[W]WW-E').startOf('day').isAfter(moment().startOf('isoWeek'))

export const getIsoWeekRangeLabel = (isoWeek: string) =>
  moment(isoWeek + '-1', 'GGGG-[W]WW-E').startOf('isoWeek').format('MM-DD-YYYY') +
  ' -- ' +
  moment(isoWeek + '-1', 'GGGG-[W]WW-E').endOf('isoWeek').format('MM-DD-YYYY')
