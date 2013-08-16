#Griddler Mobile Game Sample Data Setup

Run the following command to upload the definitions of the boards to your Griddler App Engine application:
appcfg.py upload_data --config_file=config.yml --filename=boards.csv --url=http://yourappid.appspot.com/remote_api --kind=Board
