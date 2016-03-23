create table mid2008.cars(
hhid character varying,
vmid character varying,
sourceid character varying,
a_gew numeric,
h041 character varying,
h042 character varying,
h044 character varying,
h046 character varying,
h048 character varying,
ps numeric,
kW numeric,
h0411 character varying,
h0416 character varying,
h0412 numeric,
h0413 numeric,
h0414 numeric,
fahrlj numeric,
klasse character varying,
status character varying,
seg_kba character varying,
anzfahrt numeric,
gesfkm numeric,
durchkm numeric,
gesfmin numeric,
durchmin numeric,
anzfpers numeric,
besgrad numeric,
stich_j numeric,
stich_m character varying,
stichtag character varying,
stichwo numeric,
sex_hn character varying,
alter_hn numeric,
besch_hn character varying,
hheink character varying,
oek_stat character varying,
h02 numeric,
h04_3 numeric,
hhgr06 numeric,
hhgr14 numeric,
hhgr18 numeric,
anzerw numeric,
hhtyp character varying,
bland character varying,
westost character varying,
polgk character varying,
rtyp character varying,
rtypd7 character varying,
ktyp character varying,
ktyp_zsg character varying,
sgtyp character varying,
sgtypd character varying,
psu_nr numeric
)

copy mid2008.cars from '/home/dhosse/Autos_l.csv' delimiter ';'

create view view_mid2008_cars as
select
hhid as "Haushalts-ID",
vmid as "Fahrzeug-ID",
sourceid as "Interview-Methode",
a_gew as "Gewichtungsfaktor Autos",
h041 as "Hersteller",
h042 as "Modellbezeichnung",
h044 as "Hauptnutzer (Personen-ID)",
h046 as "Zulassungsart",
h048 as "Antriebstechnologie",
ps as "PS",
kW as "kW",
h0411 as "Üblicher Stellplatz",
h0416 as "Stellplatzsuche",
h0412 as "Baujahr",
h0413 as "Anschaffungsjahr",
h0414 as "aktueller Kilometerstand",
fahrlj as "Fahrleistung [km/Jahr]",
klasse as "Typklasse (nach Hersteller und Leistung)",
status as "Statusklasse (nach Typklasse und Baujahr)",
seg_kba as "PKW-Segmenteinteilung nach KBA",
anzfahrt as "Fahrten am Stichtag",
gesfkm as "Gesamtstrecke am Stichtag [km]",
durchkm as "durchschnittliche Entfernung der Fahrten am Stichtag [km]",
gesfmin as "Gesamtfahrzeit am Stichtag [min]",
durchmin as "durchschnittliche Fahrdauer am Stichtag [min]",
anzfpers as "Anzahl beförderter Personen am Stichtag",
besgrad as "durchschnittlicher Besetzungsgrad am Stichtag",
stich_j as "Stichtag (Kalenderjahr)",
stich_m as "Stichtag (Kalendermonat)",
stichtag as "Stichtag (Wochentag)",
stichwo as "Stichtag (Kalenderwoche)",
sex_hn as "Geschlecht Hauptnutzer",
alter_hn as "Alter Hauptnutzer",
besch_hn as "Beschäftigung Hauptnutzer",
hheink as "Haushaltseinkommen [€/Monat]",
oek_stat as "ökonomischer Status",
h02 as "Anzahl Personen im Haushalt",
h04_3 as "Anzahl Autos im Haushalt",
hhgr06 as "Personen unter 6 Jahren",
hhgr14 as "Personen unter 14 Jahren",
hhgr18 as "Personen unter 18 Jahren",
anzerw as "Personen über 18 Jahren",
hhtyp as "Haushaltstyp",
bland as "Bundesland",
westost as "West-Ost-Kennung",
polgk as "politische Gemeinegröße",
rtyp as "BBSR-Regiongrundtyp",
rtypd7 as "BBSR differenzierter Regionsgrundtyp",
ktyp as "BBSR Kreistyp",
ktyp_zsg as "BBSR zuammengefasster Kreistyp",
sgtyp as "BBSR Stadt- und Gemeindetyp (4er)",
sgtypd as "BBSR differenzierter Stadt- und Gemeindetyp (7er)",
psu_nr as "laufende Nummer"
from mid2008.cars;

create table mid2008.households(
hhid character varying,
sourceid character varying,
hh_gew numeric,
h01 character varying,
h02 numeric,
p1nr numeric,
p2nr numeric,
p3nr numeric,
p4nr numeric,
p5nr numeric,
p6nr numeric,
p7nr numeric,
p8nr numeric,
hpalter1 numeric,
hpalter2 numeric,
hpalter3 numeric,
hpalter4 numeric,
hpalter5 numeric,
hpalter6 numeric,
hpalter7 numeric,
hpalter8 numeric,
hp_sex1 character varying,
hp_sex2 character varying,
hp_sex3 character varying,
hp_sex4 character varying,
hp_sex5 character varying,
hp_sex6 character varying,
hp_sex7 character varying,
hp_sex8 character varying,
hp_beru1 character varying,
hp_beru2 character varying,
hp_beru3 character varying,
hp_beru4 character varying,
hp_beru5 character varying,
hp_beru6 character varying,
hp_beru7 character varying,
hp_beru8 character varying,
hp_bkat1 character varying,
hp_bkat2 character varying,
hp_bkat3 character varying,
hp_bkat4 character varying,
hp_bkat5 character varying,
hp_bkat6 character varying,
hp_bkat7 character varying,
hp_bkat8 character varying,
hp_taet1 character varying,
hp_taet2 character varying,
hp_taet3 character varying,
hp_taet4 character varying,
hp_taet5 character varying,
hp_taet6 character varying,
hp_taet7 character varying,
hp_taet8 character varying,
hpbesch1 character varying,
hpbesch2 character varying,
hpbesch3 character varying,
hpbesch4 character varying,
hpbesch5 character varying,
hpbesch6 character varying,
hpbesch7 character varying,
hpbesch8 character varying,
hpkwfs1 character varying,
hpkwfs2 character varying,
hpkwfs3 character varying,
hpkwfs4 character varying,
hpkwfs5 character varying,
hpkwfs6 character varying,
hpkwfs7 character varying,
hpkwfs8 character varying,
h04_1 numeric,
h04_2 numeric,
h04_3 numeric,
h040_1 character varying,
h040_2 character varying,
h040_3 character varying,
h040_4 character varying,
h040_5 character varying,
h040_6 character varying,
h040_7 character varying,
h040_8 character varying,
grkpkw character varying,
hheink character varying,
oek_stat character varying,
hhgr06 numeric,
hhgr14 numeric,
hhgr18 numeric,
anzerw numeric,
hhtyp character varying,
klasse_h character varying,
status_h character varying,
fahrlj_h numeric,
co2tag_h numeric,
bland character varying,
westost character varying,
polgk character varying,
rtyp character varying,
rtypd7 character varying,
ktyp character varying,
ktyp_zsg character varying,
sgtyp character varying,
sgtypd character varying,
psu_nr numeric
);

copy mid2008.households from '/home/dhosse/Haushalte_l.csv' delimiter ';'

create view view_mid2008_households as
select
hhid as "Haushalts-ID",
sourceid as "Interview-Methode",
hh_gew as "Gewichtungsfaktor Haushalte",
h01 as "Leben Sie in Ihrem Haushalt...",
h02 as "Anzahl Personen im Haushalt",
p1nr as "Person 1",
p2nr as "Person 2",
p3nr as "Person 3",
p4nr as "Person 4",
p5nr as "Person 5",
p6nr as "Person 6",
p7nr as "Person 7",
p8nr as "Person 8",
hpalter1 as "Person 1 Alter",
hpalter2 as "Person 2 Alter",
hpalter3 as "Person 3 Alter",
hpalter4 as "Person 4 Alter",
hpalter5 as "Person 5 Alter",
hpalter6 as "Person 6 Alter",
hpalter7 as "Person 7 Alter",
hpalter8 as "Person 8 Alter",
hp_sex1 as "Person 1 Geschlecht",
hp_sex2 as "Person 2 Geschlecht",
hp_sex3 as "Person 3 Geschlecht",
hp_sex4 as "Person 4 Geschlecht",
hp_sex5 as "Person 5 Geschlecht",
hp_sex6 as "Person 6 Geschlecht",
hp_sex7 as "Person 7 Geschlecht",
hp_sex8 as "Person 8 Geschlecht",
hp_beru1 as "Person 1 berufstätig",
hp_beru2 as "Person 2 berufstätig",
hp_beru3 as "Person 3 berufstätig",
hp_beru4 as "Person 4 berufstätig",
hp_beru5 as "Person 5 berufstätig",
hp_beru6 as "Person 6 berufstätig",
hp_beru7 as "Person 7 berufstätig",
hp_beru8 as "Person 8 berufstätig",
hp_bkat1 as "Person 1 Arbeitszeit",
hp_bkat2 as "Person 2 Arbeitszeit",
hp_bkat3 as "Person 3 Arbeitszeit",
hp_bkat4 as "Person 4 Arbeitszeit",
hp_bkat5 as "Person 5 Arbeitszeit",
hp_bkat6 as "Person 6 Arbeitszeit",
hp_bkat7 as "Person 7 Arbeitszeit",
hp_bkat8 as "Person 8 Arbeitszeit",
hp_taet1 as "Person 1 Tätigkeit",
hp_taet2 as "Person 2 Tätigkeit",
hp_taet3 as "Person 3 Tätigkeit",
hp_taet4 as "Person 4 Tätigkeit",
hp_taet5 as "Person 5 Tätigkeit",
hp_taet6 as "Person 6 Tätigkeit",
hp_taet7 as "Person 7 Tätigkeit",
hp_taet8 as "Person 8 Tätigkeit",
hpbesch1 as "Person 1 Tätigkeit und Arbeitszeit",
hpbesch2 as "Person 2 Tätigkeit und Arbeitszeit",
hpbesch3 as "Person 3 Tätigkeit und Arbeitszeit",
hpbesch4 as "Person 4 Tätigkeit und Arbeitszeit",
hpbesch5 as "Person 5 Tätigkeit und Arbeitszeit",
hpbesch6 as "Person 6 Tätigkeit und Arbeitszeit",
hpbesch7 as "Person 7 Tätigkeit und Arbeitszeit",
hpbesch8 as "Person 8 Tätigkeit und Arbeitszeit",
hpkwfs1 as "Person 1 Pkw-Führerschein",
hpkwfs2 as "Person 2 Pkw-Führerschein",
hpkwfs3 as "Person 3 Pkw-Führerschein",
hpkwfs4 as "Person 4 Pkw-Führerschein",
hpkwfs5 as "Person 5 Pkw-Führerschein",
hpkwfs6 as "Person 6 Pkw-Führerschein",
hpkwfs7 as "Person 7 Pkw-Führerschein",
hpkwfs8 as "Person 8 Pkw-Führerschein",
h04_1 as "Anzahl Fahrräder",
h04_2 as "Anzahl Motorräder, Mopeds, Mofas",
h04_3 as "Anzahl Autos",
h040_1 as "kein Auto weil nicht nötig",
h040_2 as "bewusster Verzicht auf ein Auto",
h040_3 as "Auto ist zu teuer",
h040_4 as "kein Auto aus gesundheitlichen Gründen",
h040_5 as "kein Auto aus Altersgründen",
h040_6 as "kein Auto aus anderen Gründen",
h040_7 as "Aussage zu Nicht-Pkw-Besitz verweigert",
h040_8 as "weiß nicht",
grkpkw as "Hauptgrund für Nicht-Pkw-Besitz",
hheink as "Haushaltseinkommen [€/Monat]",
oek_stat as "ökonomischer Status",
hhgr06 as "Personen unter 6 Jahren",
hhgr14 as "Personen unter 14 Jahren",
hhgr18 as "Personen unter 18 Jahren",
anzerw as "Personen über 18 Jahren",
hhtyp as "Haushaltstyp",
klasse_h as "höchste Auto-Typklasse",
status_h as "höchster Auto-Status",
fahrlj_h as "Fahrleistung [km/Jahr]",
co2tag_h as "CO2-Emissionen [kg/Stichtag]",
bland as "Bundesland",
westost as "West-Ost-Kennung",
polgk as "politische Gemeindegrößenklasse",
rtyp as "BBSR Regionsgrundtyp",
rtypd7 as "BBSR differenzierter Regionstyp",
ktyp as "BBSR Kreistyp",
ktyp_zsg as "BBR zusammengefasster Kreistyp",
sgtyp as "BBSR Stadt- und Gemeindetyp (4er)",
sgtypd as "BBSR differenzierter Stadt- und Gemeindetyp (7er)",
psu_nr as "laufende Nummer"
from mid2008.households;

CREATE TABLE mid2008.persons (
hhid character varying,
pid character varying,
p_gew numeric,
int_typ9 character varying,
p01_1 character varying,
p02 character varying,
p021 character varying,
p033 character varying,
p031 character varying,
p032 character varying,
p034 character varying,
p035 character varying,
p052 character varying,
p054 character varying,
hp_sex character varying,
hp_alter character varying,
hp_altg1 character varying,
hp_altg2 character varying,
hp_altg3 character varying,
hp_beruf character varying,
hp_bkat character varying,
hp_taet character varying,
hp_besch character varying,
p14a character varying,
p14b character varying,
p14_1 character varying,
p16_1 character varying,
p0411_3 character varying,
p0411_4 character varying,
p0412_1 character varying,
p0412_2 character varying,
p0412_3 character varying,
p0412_4 character varying,
p0413_1 character varying,
p0413_2 character varying,
p0413_3 character varying,
p0413_4 character varying,
p0413 character varying,
p0414_1 character varying,
p0414_2 character varying,
p0414_3 character varying,
p0414_4 character varying,
p070 character varying,
p070_a character varying,
p10 numeric,
p06 character varying,
p061_1 character varying,
p061_2 character varying,
p061_3 character varying,
hp_pkwfs character varying,
p061_4 character varying,
p061_5 character varying,
p061_6 character varying,
p061_1j numeric,
p061_2j numeric,
p061_3j numeric,
p061_4j numeric,
"p19.1_1" character varying,
"p19.1_2" character varying,
"p19.1_3" character varying,
"p19.1_4" character varying,
"p19.1_5" character varying,
"p19.1_6" character varying,
p091_1 character varying,
p091_2 character varying,
p091_3 character varying,
p091_4 character varying,
p091_5 character varying,
p091_6 character varying,
gesein character varying,
p092 character varying,
p08 character varying,
s03 character varying,
s04 character varying,
s01 character varying,
s02_1 character varying,
s02_2 character varying,
s02_3 character varying,
s02_4 character varying,
s02_5 character varying,
s02_6 character varying,
s02_7 character varying,
mobil character varying,
rbw0 character varying,
rbw1 character varying,
rbw02 character varying,
rbw03 numeric,
rbw04 numeric,
rbw05 character varying,
w12 numeric,
wege1 numeric,
wege2 numeric,
wege3 numeric,
anzkm numeric,
anzmin numeric,
pergrup character varying,
pergrup1 character varying,
lebensph character varying,
ov_seg character varying,
co2tag_p numeric,
stich_j numeric,
stich_m character varying,
stichtag character varying,
stichwo numeric,
saison character varying,
hheink character varying,
oek_stat character varying,
h02 numeric,
h04_3 numeric,
hhgr06 numeric,
hhgr14 numeric,
hhgr18 numeric,
anzerw numeric,
hhtyp character varying,
bland character varying,
westost character varying,
polgk character varying,
rtyp character varying,
rtypd7 character varying,
ktyp character varying,
ktyp_zsg character varying,
sgtyp character varying,
sgtypd character varying,
psu_nr numeric
);

copy mid2008.persons from '/home/dhosse/Personen_l.csv' delimiter ';'

create table mid2008.journeys(
hhid character varying,
pid character varying,
rid character varying,
r_gew numeric,
p10 numeric,
p101 character varying,
p1012 character varying,
p1013_1 character varying,
p1013_2 character varying,
p1013_3 character varying,
p1013_4 character varying,
p1013_5 character varying,
p1013_6 character varying,
p1013_7 character varying,
p1013_8 character varying,
p1013_9 character varying,
hvm_r character varying,
p1014 numeric,
p1015 numeric,
p1016 numeric,
hheink character varying,
oek_stat character varying,
h02 numeric,
h04_3 numeric,
hhgr06 numeric,
hhgr14 numeric,
hhgr18 numeric,
anzerw numeric,
hhtyp character varying,
hp_sex character varying,
hp_alter numeric,
hp_altg1 character varying,
hp_altg2 character varying,
hp_altg3 character varying,
hp_besch character varying,
hp_pkwfs character varying,
pergrup character varying,
pergrup1 character varying,
lebensph character varying,
gesein character varying,
bland character varying,
westost character varying,
polgk character varying,
rtyp character varying,
rtypd7 character varying,
ktyp character varying,
ktyp_zsg character varying,
sgtyp character varying,
sgtypd character varying,
psu_nr numeric
)

copy mid2008.journeys from '/home/dhosse/Reisen_l.csv' delimiter ';'

create view view_mid2008_journeys as select
hhid as "Haushalts-ID",
pid as "Personen-ID",
rid as "Reise-ID",
r_gew as "Gewichtungsfaktor Reisen",
p10 as "Reisen mit auswärtiger Übernachtung (letzte 3 Monate)",
p101 as "hauptsächlicher Reisezweck",
p1012 as "Zielort",
p1013_1 as "Auto auf Hin- und Rückreise genutzt",
p1013_2 as "Bahn auf Hin- und Rückreise genutzt",
p1013_3 as "Reisebus auf Hin- und Rückreise genutzt",
p1013_4 as "Flugzeug auf Hin- und Rückreise genutzt",
p1013_5 as "Fahrrad auf Hin- und Rückreise genutzt",
p1013_6 as "Schiff auf Hin- und Rückreise genutzt",
p1013_7 as "sonstiges VM auf Hin- und Rückreise genutzt",
p1013_8 as "Aussage zu Verkehrsmittelnutzung verweigert",
p1013_9 as "weiß nicht",
hvm_r as "Hauptverkehrsmittel",
p1014 as "Anzahl auswärtiger Übernachtungen",
p1015 as "Anzahl Personen",
p1016 as "einfache Entfernung zu Hause - Reiseziel",
hheink as "Haushaltseinkommen [€/Monat]",
oek_stat as "ökonomischer Status",
h02 as "Anzahl Personen im Haushalt",
h04_3 as "Anzahl Autos im Haushalt",
hhgr06 as "Personen unter 6 Jahren",
hhgr14 as "Personen unter 14 Jahren",
hhgr18 as "Personen unter 18 Jahren",
anzerw as "Personen über 18 Jahren",
hp_sex as "Geschlecht",
hp_alter as "Alter",
hp_altg1 as "Altergruppe (Variante 1)",
hp_altg2 as "Altersgruppe (Variante 2)",
hp_altg3 as "Altersgruppe (Variante 3)",
hp_besch as "Tätigkeit und Arbeitszeit",
hp_pkwfs as "Pkw-Führerschein",
pergrup as "Verhaltenshomogene Personengruppe (9 Gruppen)",
pergrup1 as "Verhaltenshomogene Personengruppe (12 Gruppen)",
lebensph as "Lebensphase",
gesein as "Gesundheitliche Einschränkungen",
bland as "Bundesland",
westost as "West-Ost-Kennung",
polgk as "politische Gemeindegrößenklasse",
rtyp as "BBSR Regionsgrundtyp",
rtypd7 as "BBSR differenzierter Regionstyp",
ktyp as "BBSR Kreistyp",
ktyp_zsg as "BBR zusammengefasster Kreistyp",
sgtyp as "BBSR Stadt- und Gemeindetyp (4er)",
sgtypd as "BBSR differenzierter Stadt- und Gemeindetyp (7er)",
psu_nr as "laufende Nummer"
from mid2008.journeys;
