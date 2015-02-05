<%@page import="java.util.TimeZone"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="ie.wombat.rt.tg.TideGaugeRecord"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.List"%>

<%@page import="ie.wombat.db.HibernateUtil"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="org.hibernate.Session"%>

<%
Session hsession = HibernateUtil.currentSession();
Transaction tx = hsession.beginTransaction();

SimpleDateFormat hdf = new SimpleDateFormat("yyyyMMddHHmmss");
SimpleDateFormat df = new SimpleDateFormat("HH:mm dd MMM yyy");
TimeZone ist = TimeZone.getTimeZone("Europe/Dublin");
df.setTimeZone(ist);

Date recent = new Date (System.currentTimeMillis() - 3 * 3600000);
String query = "from TideGaugeRecord where timestamp > '"
        + hdf.format(recent)
        + "' order by timestamp desc";
List recs = hsession.createQuery(query).list();

if (recs.size() > 0) {
        TideGaugeRecord r = (TideGaugeRecord)recs.get(0);
        out.write (
        "Galway Harbour at "
        + df.format(r.getTimestamp())
        + "<br>water level: "
        + (r.getWaterElevation().doubleValue()/100)
        + "m<br>atmospheric pressure: " + r.getAtmosphericPressure()
        + "mb"
         );
}

tx.commit();
HibernateUtil.closeSession();
%>
