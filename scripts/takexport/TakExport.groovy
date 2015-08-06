package takexport
/**
 * Based on specification in https://skl-tp.atlassian.net/wiki/display/NTJP/TAK+versionshantering
 *
 * SSH tunnel when testing with riv-ta box:
 * ssh -L 3306:localhost:3306 skltp@33.33.33.33
 *
 * SSH to NTjP environments:
 * https://callistaenterprise.atlassian.net/wiki/display/Supportwiki/Databasuppkoppling+och+SQL
 *
 */

@GrabConfig(systemClassLoader=true)
@Grab(group='com.h2database', module='h2', version='1.4.187')
@Grab(group='mysql', module='mysql-connector-java', version='5.1.36')

import groovy.sql.Sql
import groovy.json.*

def username = 'root', password = 'secret', database = 'tak', server = 'localhost'

def db = Sql.newInstance("jdbc:mysql://$server/$database", username, password, 'com.mysql.jdbc.Driver')

//Streaming
def jsonWriter = new StringWriter()
def jsonBuilder = new StreamingJsonBuilder(jsonWriter)

//Non streaming
//def jsonBuilder = new JsonBuilder()

jsonBuilder{
    beskrivning "TAK versionerat format"
    formatVersion "1"
    tidpunkt new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))
    data{
        anropsadress db.rows('select * from AnropsAdress').collect{ row ->
            ["id": row.id,
            "adress": row.adress,
            "relationships":
                     ["rivtaprofil": row.rivTaProfil_id,
                      "tjanstekomponent": row.tjanstekomponent_id]]
        }

        anropsbehorighet db.rows('select * from Anropsbehorighet').collect{ row ->
            ["id": row.id,
             "fromTidpunkt": row.fromTidpunkt,
             "tomTidpunkt": row.tomTidpunkt,
             "integrationsavtal": row.integrationsavtal,
             "relationships":
                     ["logiskAdress": row.logiskAdress_id,
                      "tjanstekonsument": row.tjanstekonsument_id,
                      "tjanstekontrakt": row.tjanstekontrakt_id]]
        }

        logiskadress db.rows('select * from LogiskAdress').collect{ row ->
            ["id": row.id,
             "beskrivning": row.beskrivning,
             "hsaId": row.hsaid]
        }

        rivtaprofil db.rows('select * from RivTaProfil').collect{ row ->
            ["id": row.id,
             "beskrivning": row.beskrivning,
             "namn": row.namn]
        }

        tjanstekomponent db.rows('select * from Tjanstekomponent').collect{ row ->
            ["id": row.id,
             "beskrivning": row.beskrivning,
             "hsaId": row.hsaId]
        }

        tjanstekontrakt  db.rows('select * from Tjanstekontrakt').collect{ row ->
            ["id": row.id,
             "beskrivning": row.beskrivning,
             "majorVersion": row.majorVersion,
             "minorVersion": row.minorVersion,
             "namnrymd": row.namnrymd]
        }

        vagval db.rows('select * from Vagval').collect{ row ->
            ["id": row.id,
             "fromTidpunkt": row.fromTidpunkt,
             "tomTidpunkt": row.tomTidpunkt,
             "relationships":
                     ["anropsadress": row.anropsadress_id,
                      "logiskadress": row.logiskadress_id,
                      "tjanstekontrakt": row.tjanstekontrakt_id]]
        }
    }

}

//new File('./tak_export.json').write(JsonOutput.prettyPrint(jsonWriter.toString()))
//println jsonBuilder.prettyPrint();
println JsonOutput.prettyPrint(jsonWriter.toString())
