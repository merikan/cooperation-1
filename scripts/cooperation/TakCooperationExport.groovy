package cooperation
/**
 * This TAK export is customized to cooperation import scripts
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

println 'Starting to export data from TAK'

jsonBuilder{
    beskrivning "TAK versionerat format"
    formatVersion "1"
    tidpunkt new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))
    data{
        anropsadress db.rows(
                '''select a.id, a.adress, r.namn as rivTaProfil_id, t.hsaId as tjanstekomponent_id
                from AnropsAdress a, RivTaProfil r, Tjanstekomponent t
                where a.rivTaProfil_id = r.id
                AND a.tjanstekomponent_id = t.id'''
        ).collect{ row ->
            ["id": row.id,
            "adress": row.adress,
            "relationships":
                     ["rivtaprofil": row.rivTaProfil_id,
                      "tjanstekomponent": row.tjanstekomponent_id]]
        }

        anropsbehorighet db.rows(
                '''select a.id, a.fromTidpunkt, a.tomTidpunkt, a.integrationsavtal, l.hsaId as logiskAdress_id, t.hsaId as tjanstekonsument_id, tk.namnrymd as tjanstekontrakt_id
                from Anropsbehorighet a, LogiskAdress l, Tjanstekomponent t, Tjanstekontrakt tk
                where a.logiskAdress_id = l.id
                and a.tjanstekonsument_id = t.id
                and a.tjanstekontrakt_id = tk.id'''
        ).collect{ row ->
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

        tjanstekonsument db.rows(
                '''select distinct (tk.id), tk.beskrivning, tk.hsaId from Tjanstekomponent tk, Anropsbehorighet a
                   where tk.id = a.tjanstekonsument_id;''').collect{ row ->
            ["id": row.id,
             "beskrivning": row.beskrivning,
             "hsaId": row.hsaId]
        }

        tjansteproducent db.rows(
                '''select distinct (tk.id), tk.beskrivning, tk.hsaId from Tjanstekomponent tk, AnropsAdress a
                where tk.id = a.tjanstekomponent_id''').collect{ row ->
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

        //FIXME: För nu anges anropsadress: fysiskt url. Här behövs troligtvis en annan nyckel?
        vagval db.rows(
                '''select v.id, v.fromTidpunkt, v.tomTidpunkt, a.adress as anropsadress, l.hsaId as logiskadress_id, tk.namnrymd as tjanstekontrakt_id, riv.namn as rivTaProfil_id, t.hsaId as tjansteproducent_id
                   from Vagval v, LogiskAdress l, Tjanstekontrakt tk, AnropsAdress a, RivTaProfil riv, Tjanstekomponent t
                   where v.logiskAdress_id = l.id
                   and v.tjanstekontrakt_id = tk.id
                   and v.anropsAdress_id = a.id
                   and a.rivTaProfil_id = riv.id
                   and a.tjanstekomponent_id = t.id'''
        ).collect{ row ->
            ["id": row.id,
             "fromTidpunkt": row.fromTidpunkt,
             "tomTidpunkt": row.tomTidpunkt,
             "relationships":
                     ["logiskadress": row.logiskadress_id,
                      "tjanstekontrakt": row.tjanstekontrakt_id,
                      "tjansteproducent": row.tjansteproducent_id,
                      "rivtaProfil": row.rivTaProfil_id,
                      "anropsadress": row.anropsadress]]
        }
    }

}

new File('./tak_cooperation_export.json').write(JsonOutput.prettyPrint(jsonWriter.toString()))
println 'Done, exported TAK data to tak_cooperation_export.json'
//println jsonBuilder.prettyPrint();
//println JsonOutput.prettyPrint(jsonWriter.toString())
