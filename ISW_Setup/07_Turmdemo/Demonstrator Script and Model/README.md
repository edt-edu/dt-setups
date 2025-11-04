# Demonstrator Folder

This folder contains the work related to the demonstrator.

## Contents

- **PythonRunner.java**: Responsible for invoking the Python script `SysOnScript.py`.
- **SysOnScript.py**: Uses the SysOn API to create a new project and insert a textual SysMLv2 model. Additionally, it creates a new General View and adds basic elements to it.
- **WarehouseV3.1_General_view.sysml**: Contains a specially adapted version of the warehouse model. In this version, transitions are not included, as the primary focus of the demonstrator is on the structure. Currently, SysON has issues displaying transitions and some actions, so this version is tailored for the specific use case and is not complete, with all transitions removed. Furthermore, the use case has been extended from the previously considered 1x3 matrix to a 3x3 warehouse, which leads to inconsistent states since the 1x3 Matrix only had 9 but the 3x3 Matrix would have 81 which have not been added.
-**warehouse-db**: This is the DB-Schema of the Warehouse-DB. The link to the Document is accessible via: https://drawsql.app/teams/universitat-stuttgart-1/diagrams/warehouse-db. 
