const express = require("express");
const app = express();
const port = 3000;

app.get("/", (req, res) => res.send("Hello Santiment!"));

app.listen(port, () => console.log(`Example app listening on port ${port}!`));

setInterval(function() {
  console.log("Logging something every 10s");
}, 10000);
