package se.skltp.cooperation.takimport

import groovy.json.JsonSlurper
import groovy.sql.Sql

/**
 * Imports TAK data from TAK export JSON to cooperation database
 *
 * 1, Start your local H2 database and make sure cooperation database and tables exist
 * 2, Make sure you have exported data from TAK using TakCooperationExport.groovy
 * 3, Update the database config in this script
 * 4, Run this groovy script by $groovy TakCooperationImport.groovy
 */

 @GrabConfig(systemClassLoader=true)
 @Grab(group='com.h2database', module='h2', version='1.4.187')
 @Grab(group='org.hsqldb', module='hsqldb', version='2.3.3')
 @Grab(group='mysql', module='mysql-connector-java', version='5.1.36')


//Cooperation db settings
def username = 'sa', password = ''
def db = Sql.newInstance("jdbc:h2:tcp://localhost/~/test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", username, password, 'org.hsqldb.jdbcDriver')

//JSON file from TAK to import
def inputFile = new File("./tak_cooperation_export.json")
def inputJSON = new JsonSlurper().parseText(inputFile.text)

/* START IMPORT */

println '************************************************************'
println "File to import: $inputFile.name"
println 'Timestamp starting to import TAK data: ' + new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))
println "Format version: $inputJSON.formatVersion"
println "Description: $inputJSON.beskrivning"
println "Timestamp of exported TAK data: $inputJSON.tidpunkt"
println '************************************************************'

def countRows = { description, table ->
    def result = db.firstRow("SELECT COUNT(*) AS numberOfRows FROM " + table)
    println "$table, $description, rows: $result.numberOfRows"
}


//CONNECTION POINT
def platform = "NTjP"
def environment = 'qa'
if(db.firstRow("SELECT * FROM connectionpoint WHERE platform = $platform AND environment = $environment") == null){
    db.executeInsert "insert into connectionpoint(platform, environment)  values($platform, $environment)"
}else{
    println "INFO: Connectionpoint platform: $platform, environment: $environment already exist"
}

//LOGICAL ADDRESS
inputJSON.data.logiskadress.each{

    if(db.firstRow("SELECT * FROM logicaladdress WHERE logical_address = $it.hsaId") == null){
        db.executeInsert "insert into logicaladdress(logical_address,description)  values($it.hsaId, $it.beskrivning)"
    }else{
        println "INFO: Logical address $it already exist"
    }
}

//SERVICE CONTRACTS
countRows('BEFORE', 'servicecontract')

inputJSON.data.tjanstekontrakt.each{

    if(db.firstRow("SELECT * FROM servicecontract WHERE namespace = $it.namnrymd") == null){
        db.executeInsert "insert into servicecontract(major,minor,name, namespace)  values($it.majorVersion, $it.minorVersion, $it.beskrivning, $it.namnrymd)"
    }else{
        println "INFO: Servicecontract $it already exist"
    }
}

countRows('AFTER', 'servicecontract')

//SERVICE CONSUMERS
inputJSON.data.tjanstekonsument.each{

    if(db.firstRow("SELECT * FROM serviceconsumer WHERE hsa_id = $it.hsaId") == null){
        db.executeInsert "insert into serviceconsumer(hsa_id, description)  values($it.hsaId, $it.beskrivning)"
    }else{
        println "INFO: Serviceconsumer $it already exist"
    }
}

//SERVICE PRODUCERS
inputJSON.data.tjansteproducent.each{

    if(db.firstRow("SELECT * FROM serviceproducer WHERE hsa_id = $it.hsaId") == null){
        db.executeInsert "insert into serviceproducer(hsa_id, description)  values($it.hsaId, $it.beskrivning)"
    }else{
        println "INFO: Serviceproducer $it already exist"
    }
}

//COOPERATION
inputJSON.data.anropsbehorighet.each{

    if(db.firstRow(
            "SELECT * FROM cooperation c, logicaladdress l, serviceconsumer s, servicecontract sc, connectionpoint cp \
                WHERE c.logical_address_id = l.id \
                AND c.service_consumer_id = s.id \
                AND c.service_contract_id = sc.id \
                AND c.connection_point_id = cp.id \
                AND l.logical_address = $it.relationships.logiskAdress \
                AND s.hsa_id = $it.relationships.tjanstekonsument \
                AND sc.namespace = $it.relationships.tjanstekontrakt \
                AND cp.environment = $environment \
                AND cp.platform = $platform") == null){

        db.executeInsert \
                "insert into cooperation(connection_point_id, logical_address_id, service_consumer_id, service_contract_id) \
                    select c.id, address.id, consumer.id, contract.id \
                    from \
                        (SELECT id FROM connectionpoint WHERE platform = $platform AND environment = $environment) as c, \
                        (SELECT id FROM logicaladdress WHERE logical_address = $it.relationships.logiskAdress) as address, \
                        (SELECT id FROM serviceconsumer WHERE hsa_id = $it.relationships.tjanstekonsument) as consumer,\
                        (SELECT id FROM servicecontract WHERE namespace = $it.relationships.tjanstekontrakt) as contract"
    }else{
        println "INFO: Cooperation for serviceconsumer $it already exist"
    }
}

//SERVICEPRODUCTION

//FIXME: NOT WORKING, DOES NOT IMPORT ANY SERVICEPRODUCTION

println '*********'
println 'FIXME: DOES NOT IMPORT ANY SERVICEPRODUCTION!!'
println '*********'

inputJSON.data.vagval.each{

    if(db.firstRow(
            "SELECT * FROM serviceproduction c, logicaladdress l, serviceproducer s, servicecontract sc, connectionpoint cp \
                WHERE c.logical_address_id = l.id \
                AND c.service_producer_id = s.id \
                AND c.service_contract_id = sc.id \
                AND c.connection_point_id = cp.id \
                AND l.logical_address = $it.relationships.logiskAdress \
                AND s.hsa_id = $it.relationships.tjanstekonsument \
                AND sc.namespace = $it.relationships.tjanstekontrakt \
                AND cp.environment = $environment \
                AND cp.platform = $platform") == null){

        db.executeInsert \
                "insert into serviceproduction(physical_address, rivta_profile, connection_point_id, logical_address_id, service_producer_id, service_contract_id) \
                    select $it.relationships.anropsadress, $it.relationships.rivtaProfil, c.id, address.id, consumer.id, contract.id \
                    from \
                        (SELECT id FROM connectionpoint WHERE platform = $platform AND environment = $environment) as c, \
                        (SELECT id FROM logicaladdress WHERE logical_address = $it.relationships.logiskAdress) as address, \
                        (SELECT id FROM serviceconsumer WHERE hsa_id = $it.relationships.tjanstekonsument) as consumer,\
                        (SELECT id FROM servicecontract WHERE namespace = $it.relationships.tjanstekontrakt) as contract"
    }else{
        println "INFO: Serviceproduction already exist $it"
    }
}

println 'Done! Imported tak data to cooperation database'
