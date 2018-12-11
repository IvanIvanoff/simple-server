"use strict";

var dbm;
var type;
var seed;

/**
 * We receive the dbmigrate dependency from dbmigrate initially.
 * This enables us to not have to rely on NODE_PATH.
 */
exports.setup = (options, seedLink) => {
  dbm = options.dbmigrate;
  type = dbm.dataType;
  seed = seedLink;
};

exports.up = db => {
  return db.createTable("archives", {
    id: {
      type: "int",
      primaryKey: true,
      autoIncrement: true
    },
    archive_name: {
      type: "string",
      unique: true
    },
    timestamp: {
      type: "bigint",
      unique: true
    },
    finished: "boolean",
    start_processing: "datetime",
    end_processing: "datetime"
  });
};

exports.down = db => {
  return db.dropTable("archives");
};

exports._meta = {
  version: 1
};
