/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.bi.tools.sjukfall

import groovy.sql.Sql
import org.apache.commons.dbcp2.BasicDataSource
/**
 * Skapa SQL-fil anpassade för BI-poc av sjukfall.
 *
 * @author eriklupander
 */
class ExporteraSjukfall {

    static void main(String[] args) {

        println "- Starting Intyg -> Sjukfall creation"

        int numberOfThreads = args.length > 0 ? Integer.parseInt(args[0]) : 5
        long start = System.currentTimeMillis()
        def props = new Properties()
        new File("dataSource.properties").withInputStream { stream ->
            props.load(stream)
        }
        def config = new ConfigSlurper().parse(props)

        BasicDataSource dataSource =
            new BasicDataSource(driverClassName: config.dataSource.driver, url: config.dataSource.url,
                                username: config.dataSource.username, password: config.dataSource.password,
                                initialSize: numberOfThreads, maxTotal: numberOfThreads)
        def bootstrapSql = new Sql(dataSource)

        println("Fetching all FK7263 certificates. This may take several minutes...")
        def sjukfallRows = bootstrapSql.rows("SELECT sc.*, scwc.from_date, scwc.to_date, scwc.capacity_percentage " +
                "FROM SJUKFALL_CERT sc LEFT OUTER JOIN SJUKFALL_CERT_WORK_CAPACITY scwc ON sc.ID=scwc.CERTIFICATE_ID " +
                "ORDER BY sc.CARE_GIVER_ID, sc.ID LIMIT 500000")                                                      // , deleted : false


        bootstrapSql.close()

        println "- ${sjukfallRows.size()} candidates for being exported from sjukfall found"

        StringBuffer result = new StringBuffer()
        def lastId = null
        def minDate = null
        def maxDate = null
        def int i = 0
        sjukfallRows.collect {
            def id = it.ID


            if (id != lastId) {
                minDate = it.FROM_DATE;
                maxDate = it.TO_DATE;
                Date fdate = Date.parse("yyyy-MM-dd", minDate)
                Date ldate = Date.parse("yyyy-MM-dd", maxDate)

                Date signDateTime = it.SIGNING_DATETIME;
                int year = signDateTime[Calendar.YEAR]
                int month = signDateTime[Calendar.MONTH]
                int day = signDateTime[Calendar.DAY_OF_MONTH]
                def totalDuration = -1
                use(groovy.time.TimeCategory) {
                    def duration = ldate - fdate
                    totalDuration = duration.days
                }
                lastId = id

                result.append("INSERT INTO fact_intyg (ID,CERTIFICATE_TYPE,PATIENT_BIRTHDATE,CARE_UNIT_ID," +
                        "CARE_UNIT_NAME,CARE_GIVER_ID,SIGNING_DOCTOR_ID,SIGNING_DOCTOR_NAME,DIAGNOSE_CODE,SIGNING_DATETIME," +
                        "SIGN_YEAR,SIGN_MONTH,SIGN_DAY,TOTAL_DURATION) " +
                        "VALUES('${it.ID}','${it.CERTIFICATE_TYPE}','${it.CIVIC_REGISTRATION_NUMBER.substring(0, 8)}','${it.CARE_UNIT_ID}'," +
                        "'${it.CARE_UNIT_NAME.replace("'","")}','${it.CARE_GIVER_ID}','${it.SIGNING_DOCTOR_ID.replace("'","")}',''," +
                        "'${it.DIAGNOSE_CODE.replace("'","")}','${signDateTime}',${year},${month},${day},${totalDuration});\n")


            } else {
                def fromDate = it.FROM_DATE;
                def toDate = it.TO_DATE;
                if (minDate == null) {
                    minDate = fromDate
                } else {
                    if (fromDate < minDate) minDate = fromDate
                }
                if (maxDate == null) {
                    maxDate = toDate
                } else {
                    if (toDate > maxDate) maxDate = toDate
                }
            }
            i++
            if (i % 1000 == 0) println "Processed ${i} items"
        }

        result.append('INSERT INTO DIM_ICD (icd_code) SELECT DISTINCT(DIAGNOSE_CODE) FROM FACT_INTYG;\n');
        result.append('INSERT IGNORE INTO DIM_VARDENHET (HSA_ID,VARDENHET_NAMN) SELECT DISTINCT(CARE_UNIT_ID), CARE_UNIT_NAME FROM FACT_INTYG;\n');
        result.append('INSERT IGNORE INTO DIM_VARDGIVARE (HSA_ID,VARDGIVARE_NAMN) SELECT DISTINCT(CARE_GIVER_ID), \'\' FROM FACT_INTYG;\n');

        //println " -------- \n ${result.toString()} \n--------"

        def file1 = new File('factdata.txt')
        file1.write(result.toString())


    }

}
