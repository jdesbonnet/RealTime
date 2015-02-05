for ($i = -30; $i < 50; $i ++) {

$cmd = "wget -O t$i.png http://localhost:8080/GalwayNet/jsp/tidegauge/overlay-temp.jsp?t=$i&xx=.png";
print "$cmd\n";
system($cmd);
}

