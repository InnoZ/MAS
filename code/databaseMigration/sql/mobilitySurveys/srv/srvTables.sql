CREATE TABLE srv2013.households(
st_code integer,
st_code_name character varying,
gebiet numeric,
bundesland numeric,
teilraum numeric,
hhnr numeric,
quartal numeric,
stichtag_hh numeric,
stichtag_wtag numeric,
gewicht_hh numeric,
gewicht_hh_teilraum numeric,
ort character varying,
oberbezirk character varying,
v_anz_pers numeric,
v_anz_pkw_priv numeric,
v_anz_pkw_dienst numeric,
v_anz_mot125 numeric,
v_anz_mopmot numeric,
v_anz_rad numeric,
v_anz_erad numeric,
v_anz_sonst numeric,
v_sonst character varying,
v_fleistung1 numeric,
v_zul1 numeric,
v_stellpl1 numeric,
v_fleistung2 numeric,
v_zul2 numeric,
v_stellpl2 numeric,
v_fleistung3 numeric,
z_zul3 numeric,
v_stellpl3 numeric,
v_wohndauer numeric,
v_gehzeit_bus_hh numeric,
v_gehzeit_strab_hh numeric,
v_gehzeit_sbahn_hh numeric,
v_gehzeit_ubahn_hh numeric,
v_gehzeit_faehre_hh numeric,
v_gehzeit_nfzug_hh numeric,
v_eink numeric,
e_hhg numeric,
e_mottyp numeric,
e_hhtyp numeric,
e_anz_pkw numeric,
e_fleistung numeric
);

CREATE TABLE srv2013.persons(
st_code numeric,
st_code_name character varying,
gebiet numeric,
bundesland numeric,
teilraum numeric,
hhnr numeric,
pnr numeric,
quartal numeric,
stichtag_datum numeric,
stichtag_wtag numeric,
gewicht_p numeric,
gewicht_p_teilraum numeric,
v_alter numeric,
v_geschlecht numeric,
v_einschr_geh numeric,
v_einschr_seh numeric,
v_einschr_and numeric,
v_einschr_nein numeric,
v_einschr_ka numeric,
v_schwerbausw numeric,
v_erw numeric,
v_erw_sonst character varying,
v_schulab numeric,
v_bausb numeric,
v_fuehr_pkw numeric,
v_fuehr_mot numeric,
v_fuehr_mopmot numeric,
v_tech_handy numeric,
v_tech_smart numeric,
v_tech_inet numeric,
v_tech_navi numeric,
v_tech_kn numeric,
v_tech_ne numeric,
v_oev numeric,
v_nutzh_vm numeric,
v_nutzh_bbr numeric,
v_nutzh_bbls numeric,
v_oev_fk numeric,
v_oev_fk_sonst character varying,
v_oev_fk_ffm numeric,
v_oev_fk_sonst_ffm character varying,
v_oev_fk_mdv numeric,
v_oev_fk_sonst_mdv character varying,
v_mdv_fk numeric,
v_oev_fk_vvo numeric,
v_oev_fk_sonst_vvo character varying,
v_oev_fk_rmv numeric,
v_oev_fk_sonst_rmv character varying,
v_nutzh_pkw numeric,
v_2nutzh_pkw numeric,
v_nutzh_rad numeric,
v_2nutzh_rad numeric,
v_stellpl_rad numeric,
v_mietrad_p numeric,
v_carshare numeric,
v_nutzh_bus_apl numeric,
v_nutzh_bus_bild numeric,
v_nutzh_bus_ek numeric,
v_nutzh_bus_frei numeric,
v_vinfo_net numeric,
v_vinfo_face numeric,
v_vinfo_tel numeric,
v_vinfo_cen numeric,
v_vinfo_verk numeric,
v_vinfo_hst numeric,
v_vinfo_fpb numeric,
v_vinfo_bek numeric,
v_vinfo_kn numeric,
v_vinfo_ne numeric,
v_massn_haeuf numeric,
v_massn_wohn numeric,
v_massn_ziel numeric,
v_massn_abst numeric,
v_massn_sbahn numeric,
v_massn_puenk numeric,
v_mass_saub numeric,
v_busan_haeuf numeric,
v_busan_reisez numeric,
v_busan_kenntn numeric,
v_busan_entf numeric,
v_busan_abst numeric,
v_gehzeit_bus_p numeric,
v_gehzeit_strab_p numeric,
v_gehzeit_sbahn_p numeric,
v_gehzeit_ubahn_p numeric,
v_gehzeit_faehre_p numeric,
v_gehzeit_nfzug_p numeric,
v_erapl_fuss numeric,
v_erapl_rad numeric,
v_erapl_pkw numeric,
v_erapl_bbr numeric,
v_erek_fuss numeric,
v_erek_rad numeric,
v_erek_pkw numeric,
v_erek_bbr numeric,
v_normalitaet numeric,
v_pkw_verfueg numeric,
v_rad_verfueg numeric,
v_erad_verfueg numeric,
v_fk_verfueg numeric,
v_wetter_sonne numeric,
v_wetter_wolkig numeric,
v_wetter_bedeckt numeric,
v_wetter_regen numeric,
v_wetter_schnee numeric,
v_wetter_wn numeric,
v_wohnort numeric,
v_wohnung numeric,
v_wohnung_grund numeric,
v_wohnung_grund_sonst character varying,
v_berufwege numeric,
v_ap1_zweck numeric,
v_ap1_zweck_sonst numeric,
e_alter_5 numeric,
e_erw_3 numeric,
e_wetter numeric,
e_mobil numeric,
e_mobil2 numeric,
e_anz_wege numeric,
e_anz_wege2 numeric,
e_verkehrsleist numeric
);

CREATE TABLE srv2013.ways(
st_code numeric,
st_code_name character varying,
gebiet numeric,
bundesland numeric,
teilraum numeric,
hhnr numeric,
pnr numeric,
wnr numeric,
ausgang numeric,
quartal numeric,
stichtag_datum numeric,
stichtag_wtag numeric,
gewicht_w numeric,
gewicht_w_teilraum numeric,
v_start_lage numeric,
v_start_ort character varying,
v_start_land character varying,
v_start_oberbezirk numeric,
v_beginn_stunde numeric,
v_beginn_minute numeric,
v_ankunft_stunde numeric,
v_ankunft_minute numeric,
v_zweck numeric,
v_zweck_sonst character varying,
v_zweck_bhol numeric,
v_zweck_bhol_sonst character varying,
v_begleitung_ohne numeric,
v_begleitung_hh numeric,
v_begleitung_and numeric,
v_fuss numeric,
v_rad numeric,
v_erad numeric,
v_leihrad numeric,
v_mop numeric,
v_hhpkw_f numeric,
v_andpkw_f numeric,
v_carsharing_f numeric,
v_hhpkw_mf numeric,
v_andpkw_mf numeric,
v_carsharing_mf numeric,
v_bus numeric,
v_strab numeric,
v_ubahn numeric,
v_sbahn numeric,
v_nzug numeric,
v_fzug numeric,
v_taxi numeric,
v_vm_and numeric,
v_vm_and_tx character varying,
v_vm_unplaus numeric,
v_vm_laeng numeric,
v_vm_reihe_1 numeric,
v_vm_reihe_2 numeric,
v_vm_reihe_3 numeric,
v_vm_reihe_4 numeric,
v_vm_reihe_5 numeric,
v_vm_reihe_6 numeric,
v_vm_reihe_7 numeric,
v_vm_reihe_8 numeric,
v_f_anzahl numeric,
v_ziel_lage numeric,
v_ziel_ort character varying,
v_ziel_land character varying,
v_ziel_oberbezirk numeric,
v_laenge numeric,
e_beginn numeric,
e_ankunft numeric,
e_hvm numeric,
e_hvm_4 numeric,
e_rva numeric,
e_dauer numeric,
e_geschw numeric,
e_weg_gueltig numeric,
e_zweck_obhol numeric,
estart_zweck_obhol numeric,
e_zweck_oheim numeric,
e_zweck_obhol_oheim numeric
);
