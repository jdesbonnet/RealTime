for ($i = 0; $i <= 100; $i ++) {

$cmd = "wget -O rh$i.png http://localhost:8080/GalwayNet/jsp/tidegauge/overlay-rh.jsp?rh=$i&xx=.png";
print "$cmd\n";
system($cmd);
}

