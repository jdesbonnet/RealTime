<%
	String SEP = "\t";
	if ( request.getMethod().equals("GET") ) {	
%>
<form method="POST">
<textarea name="data" rows="20" cols="40"></textarea>
<br />
<input type="submit" value="Submit" /></form>
<%
	} else {
		
		response.setContentType("text/plain");
		
		int[][] records = new int[40][8];
		String[] lines = request.getParameter("data").split("\r\n");
		for (String line : lines) {
			if ( ! (line.startsWith("R") || line.startsWith("W"))) {
				continue;
			}
			String[] p = line.split(" ");
			int addr = Integer.parseInt(p[1],16);
			int value = Integer.parseInt(p[2],16);
			// Addresses under 16 are not for BP readings
			if (addr < 16) {
				continue;
			}
			int recordIndex = (addr - 16)/8;
			int col = addr%8;
			records[recordIndex][col] = value;
		}
		
		for (int i = 0; i < 40; i++) {
			if (records[i][0] == 0) {
				continue;
			}
			
			// European format
			//out.write (records[i][1] + "/" + records[i][0] + "/2010 ");
			
			// US/spreadsheet format
			out.write (records[i][0] + "/" + records[i][1] + "/2010 ");
			
			// Hours
			// Stored in 12 hour clock value (lower nibble) and bit 7 = PM flag.
			int h = records[i][2];
			h =  h > 127  ?  (h & 0xf) + 12 : (h & 0xf);
			
			if (h < 10) {
				out.write ("0");
			}
			out.write (h + ":");
			
			// Minutes
			int m = records[i][3];
			if (m < 10) {
				out.write ("0");
			}
			out.write ("" + m + SEP);
			
			// Systolic
			int sys = ((records[i][4] & 0xf0)>>4) * 100;
			sys +=  ((records[i][5] & 0xf0)>>4) * 10;
			sys +=  ((records[i][5] & 0x0f));
			out.write ("" + sys + SEP);
			
			// Diastolic
			int dia =  (records[i][4] & 0x0f) * 100;
			dia +=  ((records[i][6] & 0xf0)>>4) * 10;
			dia +=  ((records[i][6] & 0x0f));
			out.write ("" + dia + SEP);
			
			// Heart rate
			out.write ("" + records[i][7]);
			out.write ("\n");
		}
	}
%>