create view Haushalte_MiD2008 as
select
hhid as "Haushalts-ID",
sourceid as "Methode des Haushalts-Interviews",
hh_gew as "Gewichtungsfaktor Haushate"
from households