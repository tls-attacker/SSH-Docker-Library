import unittest
from appium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.keys import Keys

class TeraTermSSHClientTest(unittest.TestCase):

    def __init__(self, methodName='runTest', hostname=None, username=None, port=None, debug=None, repeat_count=1):
        super(TeraTermSSHClientTest, self).__init__(methodName)
        # In this case we want to use class attributes, as for setUpClass() we need to be able to refer to them
        #Todo unfortunately this destroys the initial idea of parallelizing, but because of client command in ECDSA-Sampler there might not be a need for parallelization
        self.hostname = hostname
        self.username = username
        self.port = port
        self.debug = debug
        self.repeat_count = repeat_count


    def setUp(self):
        executable_path = r'C:\Program Files (x86)\teraterm5\ttermpro.exe'
        arguments=self.username+ '@'+self.hostname+':'+str(self.port)
        desired_caps = {
            'platformName': 'Windows',
            'deviceName': 'WindowsPC',
            'app': executable_path,
            'appArguments': arguments
        }
        self.driver = webdriver.Remote(
            command_executor='http://127.0.0.1:4723',
            desired_capabilities=desired_caps
        )


    def test_publickey_auth(self):
        def debug_print(message):
                if self.debug:
                        print(message)
            # Click OK button to start connection
        init_con_button = WebDriverWait(self.driver, 30).until(
            EC.presence_of_element_located((By.NAME, 'OK'))
        )
        self.assertIsNotNone(init_con_button)
        init_con_button.click()
        debug_print("Connection initialized...")
        #press closing button and confirm
        close_button = WebDriverWait(self.driver, 30).until(
            EC.presence_of_element_located((By.NAME, 'Schließen'))
        )
        self.assertIsNotNone(close_button)
        close_button.click()
        # Click OK button to confirm closing the terminal
        confirm_button = WebDriverWait(self.driver, 30).until(
            EC.presence_of_element_located((By.NAME, 'OK'))
        )
        self.assertIsNotNone(confirm_button)
        confirm_button.click()
