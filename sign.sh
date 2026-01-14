apksigner sign \
  --ks /home/willie/willie-release-key.jks \
  --ks-key-alias willie-key \
  --ks-pass pass:$PASSWD \
  --key-pass pass:$PASSWD \
  --out com.willie.mancala_$CODE.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk

