create table mid2002.households(
CASEID character varying,
H01 character varying,
H001C character varying,
H02 numeric,
H1SEX character varying,
H2SEX character varying,
H3SEX character varying,
H4SEX character varying,
H5SEX character varying,
H6SEX character varying,
H7SEX character varying,
H8SEX character varying,
H1ALTER numeric,
H2ALTER numeric,
H3ALTER numeric,
H4ALTER numeric,
H5ALTER numeric,
H6ALTER numeric,
H7ALTER numeric,
H8ALTER numeric,
H1BERUF character varying,
H2BERUF character varying,
H3BERUF character varying,
H4BERUF character varying,
H5BERUF character varying,
H6BERUF character varying,
H7BERUF character varying,
H8BERUF character varying,
H1BESCH character varying,
H2BESCH character varying,
H3BESCH character varying,
H4BESCH character varying,
H5BESCH character varying,
H6BESCH character varying,
H7BESCH character varying,
H8BESCH character varying,
H1TAET character varying,
H2TAET character varying,
H3TAET character varying,
H4TAET character varying,
H5TAET character varying,
H6TAET character varying,
H7TAET character varying,
H8TAET character varying,
H1FHS character varying,
H2FHS character varying,
H3FHS character varying,
H4FHS character varying,
H5FHS character varying,
H6FHS character varying,
H7FHS character varying,
H8FHS character varying,
H041_1 character varying,
H0412_1 numeric,
H0413_1 numeric,
H044_1 character varying,
H045_1 character varying,
H046_1 character varying,
H047_1 character varying,
H048_1 character varying,
H049_1 character varying,
H0491_1 numeric,
H0492_1 numeric,
H0410_1 numeric,
H0411_11 character varying,
H0411_21 character varying,
H0411_31 character varying,
H0411_41 character varying,
H0414_1 numeric,
H0415_1 numeric,
H041_2 character varying,
H0412_2 numeric,
H0413_2 numeric,
H044_2 character varying,
H045_2 character varying,
H046_2 character varying,
H047_2 character varying,
H048_2 character varying,
H049_2 character varying,
H0491_2 numeric,
H0492_2 numeric,
H0410_2 numeric,
H0411_12 character varying,
H0411_22 character varying,
H0411_32 character varying,
H0411_42 character varying,
H0414_2 numeric,
H0415_2 numeric,
H041_3 character varying,
H0412_3 numeric,
H0413_3 numeric,
H044_3 character varying,
H045_3 character varying,
H046_3 character varying,
H047_3 character varying,
H048_3 character varying,
H049_3 character varying,
H0491_3 numeric,
H0492_3 numeric,
H0410_3 numeric,
H0411_13 character varying,
H0411_23 character varying,
H0411_33 character varying,
H0411_43 character varying,
H0414_3 numeric,
H0415_3 numeric,
H04_1 numeric,
H04_2 numeric,
H04_3 numeric,
H04_4 numeric,
H04_5 numeric,
H04_6 numeric,
H05 character varying,
H06_1 character varying,
H06_2 character varying,
H06_3 character varying,
H06_4 character varying,
H06_5 character varying,
H06_7 character varying,
H06_8 character varying,
H07 character varying,
H071 character varying,
H071C character varying,
H0712C character varying,
H0713C character varying,
H072 character varying,
H072C character varying,
H0722C character varying,
H0723C character varying,
HHEINK character varying,
WTAG character varying,
BLAND character varying,
WESTOST character varying,
BBR_REG character varying,
BBR_DREG character varying,
BBRKRTYP character varying,
GGKPOL character varying,
GGKBIK character varying,
VOLL_1 numeric,
VOLL_50 numeric,
VOLL_100 numeric,
ERWB1 character varying,
ERWB2 character varying,
ERWB3 character varying,
ERWB4 character varying,
ERWB5 character varying,
ERWB6 character varying,
ERWB7 character varying,
ERWB8 character varying,
LEBENSP character varying,
P_INT numeric,
P_INTGR character varying,
HHGR06 numeric,
HHGR14 numeric,
HHGR18 numeric,
GEW_HB numeric,
STICHTAG numeric,
GEMNR numeric,
STICHPRO character varying,
BEFRAG character varying
);

copy mid2002.households from '/home/dhosse/MiD/2002/Haushalte_l.csv' DELIMITER ';'

create view view_mid2002_households as select
CASEID as "Haushalts-ID",
H01 as "Personen im Haushalt",
H001C as "Unterlagen zu Studie erhalten?",
H02 as "Haushaltsgröße",
H1SEX as "Person 1 Geschlecht",
H2SEX as "Person 2 Geschlecht",
H3SEX as "Person 3 Geschlecht",
H4SEX as "Person 4 Geschlecht",
H5SEX as "Person 5 Geschlecht",
H6SEX as "Person 6 Geschlecht",
H7SEX as "Person 7 Geschlecht",
H8SEX as "Person 8 Geschlecht",
H1ALTER as "Person 1 Alter",
H2ALTER as "Person 2 Alter",
H3ALTER as "Person 3 Alter",
H4ALTER as "Person 4 Alter",
H5ALTER as "Person 5 Alter",
H6ALTER as "Person 6 Alter",
H7ALTER as "Person 7 Alter",
H8ALTER as "Person 8 Alter",
H1BERUF as "Person 1 Berufstätigkeit",
H2BERUF as "Person 2 Berufstätigkeit",
H3BERUF as "Person 3 Berufstätigkeit",
H4BERUF as "Person 4 Berufstätigkeit",
H5BERUF as "Person 5 Berufstätigkeit",
H6BERUF as "Person 6 Berufstätigkeit",
H7BERUF as "Person 7 Berufstätigkeit",
H8BERUF as "Person 8 Berufstätigkeit",
H1BESCH as "Person 1 Beschäftigungsart",
H2BESCH as "Person 2 Beschäftigungsart",
H3BESCH as "Person 3 Beschäftigungsart",
H4BESCH as "Person 4 Beschäftigungsart",
H5BESCH as "Person 5 Beschäftigungsart",
H6BESCH as "Person 6 Beschäftigungsart",
H7BESCH as "Person 7 Beschäftigungsart",
H8BESCH as "Person 8 Beschäftigungsart",
H1TAET as "Person 1 Tätigkeit",
H2TAET as "Person 2 Tätigkeit",
H3TAET as "Person 3 Tätigkeit",
H4TAET as "Person 4 Tätigkeit",
H5TAET as "Person 5 Tätigkeit",
H6TAET as "Person 6 Tätigkeit",
H7TAET as "Person 7 Tätigkeit",
H8TAET as "Person 8 Tätigkeit",
H1FHS as "Person 1 Pkw-Führerscheinbesitz",
H2FHS as "Person 2 Pkw-Führerscheinbesitz",
H3FHS as "Person 3 Pkw-Führerscheinbesitz",
H4FHS as "Person 4 Pkw-Führerscheinbesitz",
H5FHS as "Person 5 Pkw-Führerscheinbesitz",
H6FHS as "Person 6 Pkw-Führerscheinbesitz",
H7FHS as "Person 7 Pkw-Führerscheinbesitz",
H8FHS as "Person 8 Pkw-Führerscheinbesitz",
H041_1 as "Pkw 1 Hersteller",
H0412_1 as "Pkw 1 Baujahr",
H0413_1 as "Pkw 1 Erwerbsjahr",
H044_1 as "Pkw 1 Hauptnutzer",
H045_1 as "Pkw 1 Nutzer Person im Haushalt",
H046_1 as "Pkw 1 Halter",
H047_1 as "Pkw 1 Zulassungsart",
H048_1 as "Pkw 1 Antriebsart",
H049_1 as "Pkw 1 PS- / kW-Angabe Einheit",
H0491_1 as "Pkw 1 PS",
H0492_1 as "Pkw 1 kW",
H0410_1 as "Pkw 1 Hubraum [ccm]",
H0411_11 as "Pkw 1 Stellplatz Garage",
H0411_21 as "Pkw 1 Stellplatz Parkplatz am Haus",
H0411_31 as "Pkw 1 Stellplatz Parkplatz in der Nähe",
H0411_41 as "Pkw 1 Stellplatz unterschiedlich",
H0414_1 as "Pkw 1 Kilometerstand",
H0415_1 as "Pkw 1 Jahresfahrleistung [km]",
H041_2 as "Pkw 2 Hersteller",
H0412_2 as "Pkw 2 Baujahr",
H0413_2 as "Pkw 2 Erwerbsjahr",
H044_2 as "Pkw 2 Hauptnutzer",
H045_2 as "Pkw 2 Nutzer Person im Haushalt",
H046_2 as "Pkw 2 Halter",
H047_2 as "Pkw 2 Zulassungsart",
H048_2 as "Pkw 2 Antriebsart",
H049_2 as "Pkw 2 PS- / kW-Angabe Einheit",
H0491_2 as "Pkw 2 PS",
H0492_2 as "Pkw 2 kW",
H0410_2 as "Pkw 2 Hubraum [ccm]",
H0411_12 as "Pkw 2 Stellplatz Garage",
H0411_22 as "Pkw 2 Stellplatz Parkplatz am Haus",
H0411_32 as "Pkw 2 Stellplatz Parkplatz in der Nähe",
H0411_42 as "Pkw 2 Stellplatz unterschiedlich",
H0414_2 as "Pkw 2 Kilometerstand",
H0415_2 as "Pkw 2 Jahresfahrleistung [km]",
H041_3 as "Pkw 3 Hersteller",
H0412_3 as "Pkw 3 Baujahr",
H0413_3 as "Pkw 3 Erwerbsjahr",
H044_3 as "Pkw 3 Hauptnutzer",
H045_3 as "Pkw 3 Nutzer Person im Haushalt",
H046_3 as "Pkw 3 Halter",
H047_3 as "Pkw 3 Zulassungsart",
H048_3 as "Pkw 3 Antriebsart",
H049_3 as "Pkw 3 PS- / kW-Angabe Einheit",
H0491_3 as "Pkw 3 PS",
H0492_3 as "Pkw 3 kW",
H0410_3 as "Pkw 3 Hubraum [ccm]",
H0411_13 as "Pkw 3 Stellplatz Garage",
H0411_23 as "Pkw 3 Stellplatz Parkplatz am Haus",
H0411_33 as "Pkw 3 Stellplatz Parkplatz in der Nähe",
H0411_43 as "Pkw 3 Stellplatz unterschiedlich",
H0414_3 as "Pkw 3 Kilometerstand",
H0415_3 as "Pkw 3 Jahresfahrleistung [km]",
H04_1 as "Anzahl Fahrräder im Haushalt",
H04_2 as "Anzahl Motorräder, Mofas etc. im Haushalt",
H04_3 as "Anzahl Pkw im Haushalt",
H04_4 as "Anzahl Lkw bis 3,5t im Haushalt",
H04_5 as "Anzahl Lkw ab 3,5t im Haushalt",
H04_6 as "Anzahl Lkw gesamt im Haushalt",
H05 as "Wohnlage",
H06_1 as "technische Einrichtungen: Telefon Festnetz verfügbar",
H06_2 as "technische Einrichtungen: Handy verfügbar",
H06_3 as "technische Einrichtungen: Computer verfügbar",
H06_4 as "technische Einrichtungen: Internet verfügbar",
H06_5 as "technische Einrichtungen: nichts davon",
H06_7 as "technische Einrichtungen: verweigert",
H06_8 as "technische Einrichtungen: weiß nicht",
H07 as "Netto-Haushaltseinkommen Euro oder DM",
H071 as "Netto-Haushaltseinkommen [DM]",
H071C as "Netto-Haushaltseinkommen [DM] 1. Nachfrage",
H0712C as "Netto-Haushaltseinkommen [DM], 2. Nachfrage",
H0713C as "Netto-Haushaltseinkommen [DM], 3. Nachfrage",
H072 as "Netto-Haushaltseinkommen [€]",
H072C as "Netto-Haushaltseinkommen [€], 1. Nachfrage",
H0722C as "Netto-Haushaltseinkommen [€], 2. Nachfrage",
H0723C as "Netto-Haushaltseinkommen [€], 3. Nachfrage",
HHEINK as "Haushaltseinkommen [€]",
WTAG as "Wochtentag",
BLAND as "Bundesland",
WESTOST as "West-Ost-Kennung",
BBR_REG as "Regionsgrundtyp",
BBR_DREG as "differenzierter Regionstyp",
BBRKRTYP as "Kreistyp",
GGKPOL as "politische Ortsgrößenklasse",
GGKBIK as "BIK-Regionstypklasse",
VOLL_1 as "1-Personenregel",
VOLL_50 as "50%-Personenregel",
VOLL_100 as "100%-Personenregel",
ERWB1 as "Person 1 Tätigkeit (rekodiert)",
ERWB2 as "Person 2 Tätigkeit (rekodiert)",
ERWB3 as "Person 3 Tätigkeit (rekodiert)",
ERWB4 as "Person 4 Tätigkeit (rekodiert)",
ERWB5 as "Person 5 Tätigkeit (rekodiert)",
ERWB6 as "Person 6 Tätigkeit (rekodiert)",
ERWB7 as "Person 7 Tätigkeit (rekodiert)",
ERWB8 as "Person 8 Tätigkeit (rekodiert)",
LEBENSP as "Lebensphase",
P_INT as "Anzahl realisierter Personeninterviews",
P_INTGR as "Anzahl Personeninterviews im HH gruppiert",
HHGR06 as "Personen unter 6 Jahren",
HHGR14 as "Personen unter 14 Jahren",
HHGR18 as "Personen unter 18 Jahren",
GEW_HB as "Haushaltsgewicht",
STICHTAG as "Stichtag",
GEMNR as "Gemeindenummer",
STICHPRO as "Stichprobenkennung",
BEFRAG as "Befragungsart"
from mid2002.households;

create table mid2002.persons(
CASEID character varying,
PID character varying,
P01 numeric,
P02 numeric,
P021 numeric,
P022 numeric,
P033 numeric,
P031 numeric,
P032 numeric,
P034 numeric,
P051 numeric,
P052 numeric,
P053 numeric,
P054 numeric,
P041 numeric,
P042 numeric,
P07 numeric,
P10AS numeric,
P10 numeric,
P101_1 numeric,
P101_2 numeric,
P101_3 numeric,
P101_4 numeric,
P102_1 numeric,
P102_2 numeric,
P102_3 numeric,
P102_4 numeric,
P102_5 numeric,
P102_6 numeric,
P11 numeric,
P12_1 numeric,
P12_2 numeric,
P12_3 numeric,
P12_4 numeric,
P12_5 numeric,
P13 numeric,
P06 numeric,
P061_1 numeric,
P061_2 numeric,
P061_3 numeric,
P061_4 numeric,
P061_1J numeric,
P061_2J numeric,
P061_3J numeric,
P061_4J numeric,
P14 numeric,
P15 numeric,
P16 numeric,
P17 numeric,
P18 numeric,
P17S numeric,
P18S numeric,
P09_1 numeric,
P09_2 numeric,
P09_3 numeric,
P09_4 numeric,
P09 numeric,
P08 numeric,
S03 numeric,
S04 numeric,
S05 numeric,
S01 numeric,
S02_1 numeric,
S02_2 numeric,
S02_3 numeric,
S02_4 numeric,
S02_5 numeric,
S02_6 numeric,
S02_7 numeric,
S02_8 numeric,
WV0 numeric,
WV01 numeric,
WV02 numeric,
WV03 numeric,
WV04 numeric,
WV05 numeric,
W12 numeric,
WEGANZ_1 numeric,
WEGANZ_2 numeric,
WEGANZ_3 numeric,
H02 numeric,
H05 numeric,
HHEINK numeric,
LEBENSP numeric,
OV_SEG numeric,
PERGRUP numeric,
PERGRUP1 numeric,
CASEINFO numeric,
PROXY numeric,
PERSINFO numeric,
VOLL_HH numeric,
BLAND numeric,
WESTOST numeric,
GGKPOL numeric,
GGKBIK numeric,
BBR_REG numeric,
BBR_DREG numeric,
BBRKRTYP numeric,
WTAG numeric,
WEGDAUER numeric,
H04_3 numeric,
H04_6 numeric,
HHGR06 numeric,
HHGR14 numeric,
HHGR18 numeric,
WEGTEMPO numeric,
KMINSGK numeric,
GEW_PB numeric,
STICHTAG numeric,
PALTER numeric,
PSEX numeric,
KINFO numeric,
MOBIL numeric,
GEMNR numeric,
FRAGEBOG numeric
);

copy mid2002.persons from '/home/dhosse/MiD/2002/Personen.csv' DELIMITER ';'

create table mid2002.ways(
CASEID character varying,
PID character varying,
WEG character varying,
W01 numeric,
w111 numeric,
w112 numeric,
w04 numeric,
w041 numeric,
w042 numeric,
w043 numeric,
w13 numeric,
w05_1 numeric,
w05_2 numeric,
w05_3 numeric,
w05_4 numeric,
w05_5 numeric,
w05_6 numeric,
w05_7 numeric,
w05_7s numeric,
w05_8 numeric,
w05_9 numeric,
w05_9s numeric,
w05_10 numeric,
w05_11 numeric,
w05_12 numeric,
w05_13 numeric,
w05_14 numeric,
w05_15 numeric,
w05_16 numeric,
w05_17 numeric,
w05_18 numeric,
w05_20 numeric,
w05_21 numeric,
w061 numeric,
w062 numeric,
w063 numeric,
w07s numeric,
w07 numeric,
w071 numeric,
w0721 numeric,
w0722 numeric,
w0723 numeric,
w0724 numeric,
w0725 numeric,
w0726 numeric,
w0727 numeric,
w0728 numeric,
w0729 numeric,
w073 numeric,
w08 numeric,
w03 numeric,
w03_hs numeric,
w03_ms numeric,
w09 numeric,
w09_hs numeric,
w09_ms numeric,
wegdauer numeric,
hpzweck numeric,
diwzw1 numeric,
diwzw2 numeric,
diwzw3 numeric,
w05 numeric,
erfwege numeric,
weganz_1 numeric,
weganz_2 numeric,
weganz_3 numeric,
hheink numeric,
lebensp numeric,
ov_seg numeric,
pergrup numeric,
pergrup1 numeric,
caseinfo numeric,
proxy numeric,
voll_hh numeric,
bland numeric,
westost numeric,
ggkpol numeric,
ggkbik numeric,
bbr_reg numeric,
bbr_dreg numeric,
bbrkrtyp numeric,
wtag numeric,
p12_1 numeric,
p12_2 numeric,
p12_3 numeric,
p12_4 numeric,
p12_5 numeric,
p01 numeric,
p02 numeric,
p051 numeric,
p052 numeric,
p053 numeric,
p054 numeric,
p041 numeric,
p042 numeric,
p11 numeric,
p13 numeric,
p14 numeric,
p15 numeric,
p16 numeric,
p17 numeric,
p18 numeric,
p17s numeric,
p18s numeric,
p09_1 numeric,
p09_2 numeric,
p09_3 numeric,
p09_4 numeric,
p09 numeric,
ks03 numeric,
ks04 numeric,
ks01 numeric,
h02 numeric,
h05 numeric,
h04_3 numeric,
h04_6 numeric,
hhgr06 numeric,
hhgr14 numeric,
hhgr18 numeric,
wegtempo numeric,
gew_pb numeric,
gew_wb numeric,
stichtag numeric,
palter numeric,
psex numeric,
persinfo numeric,
gemnr numeric,
stichpro numeric,
fragebog numeric
);

copy mid2002.ways from '/home/dhosse/MiD/2002/Wege.csv' DELIMITER ';'

create table mid2002.cars(
caseid character varying,
fzgid character varying,
pid character varying,
herstell text,
baujahr numeric,
erwerbsj numeric,
halter numeric,
zulass numeric,
antr_art numeric,
hubraum numeric,
ps numeric,
kw numeric,
km_stand numeric,
jahrflst numeric,
npersfah numeric,
nfzgfahr numeric,
persmin numeric,
perskm numeric,
npersmin numeric,
nperskm numeric,
fzgmin numeric,
fzgkm numeric,
nfzgmin numeric,
nfzgkm numeric,
h0411_1 numeric,
h0411_2 numeric,
h0411_3 numeric,
h0411_4 numeric,
h02 numeric,
h04_3 numeric,
wv0 numeric,
wv01 numeric,
wv02 numeric,
wv03 numeric,
wv04 numeric,
wv05 numeric,
plebzykl numeric,
palter numeric,
psex numeric,
hpbesch numeric,
hptaet numeric,
lebensp numeric,
gew_hb numeric,
basisauf numeric,
hheink numeric,
wtag numeric,
westost numeric,
switcher numeric,
stichtag numeric,
bland numeric,
btag numeric,
bjahr numeric,
bmonat numeric,
tagtyp numeric,
bwotag numeric,
bwoche numeric,
hgew numeric
);

copy mid2002.cars from '/home/dhosse/MiD/2002/Pkw.csv' DELIMITER ';'
