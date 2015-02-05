<table width="150" border="1" cellpadding="1" cellspacing="0">
<thead>
<tr>
<td colspan="3">
Wind Speeds
</td>
<tr>
<td></td>
<td>kn</td>
<td>km/h</td>
</tr>
<%
float skmh;
for (int i = 0; i < 80; i+=5) {
	skmh = (float)i * 1.852f; 
%>
<tr>
<td>
<img alt="Wind barb for <%=i %>kn" src="overlay-windbarb.jsp?w=48&h=48&d=90&s=<%=skmh %>&xx=.png"
width="48" height="32" />
</td>
<td align="right"><%=i %></td>
<td align="right"><%=(int)skmh %></td>
</tr>
<%
}
%>
</table>
