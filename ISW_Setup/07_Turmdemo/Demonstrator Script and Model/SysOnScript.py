import requests
import json
import uuid
import traceback
import sys
import webbrowser
import subprocess
import os
import time
import pygetwindow as gw
import ctypes

textual_model = sys.stdin.read() 
print(f"String received: {textual_model}")

BASE_URL = "http://localhost:8080"
GRAPHQL_URL = f"{BASE_URL}/api/graphql"
REST_PROJECT_URL = f"{BASE_URL}/api/rest/projects"

HEADERS = {
    "accept": "*/*",
    "accept-encoding": "gzip, deflate, br, zstd",
    "accept-language": "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7",
    "content-type": "application/json",
    "origin": "http://localhost:8080",
    "referer": "http://localhost:8080/",
    "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36",
    "sec-ch-ua": '"Chromium";v="136", "Google Chrome";v="136", "Not.A/Brand";v="99"',
    "sec-ch-ua-mobile": "?0",
    "sec-ch-ua-platform": '"Windows"',
    "sec-fetch-dest": "empty",
    "sec-fetch-mode": "cors",
    "sec-fetch-site": "same-origin",
}

def create_project(name, description):
    """
    Creates a new project using the REST API.
    """
    print("[1] Creating project...")
    url = f"{REST_PROJECT_URL}?name={name}&description={description}"
    try:
        response = requests.post(url, headers={"accept": "application/json"}, data='')
        response.raise_for_status()
        project_data = response.json()
        project_id = project_data.get("@id")
        if not project_id:
            raise ValueError("Project ID not found in response.")
        print(f"Project created with ID: {project_id}")
        return project_id
    except requests.exceptions.RequestException as e:
        print(f"Error while creating project: {e}")
        if hasattr(e, 'response') and e.response is not None:
            print(f"Server response: {e.response.text}")
        raise

def get_editing_context_id(project_id):
    """
    Queries the current editing context via GraphQL.
    """
    print("[2] Fetching EditingContextId...")

    query = '''
    query getProjectEditingContext($projectId: ID!) {
      viewer {
        project(projectId: $projectId) {
          currentEditingContext {
            id
          }
        }
      }
    }
    '''
    variables = {
        "projectId": project_id
    }

    payload = {
        "operationName": "getProjectEditingContext",
        "query": query,
        "variables": variables
    }

    try:
        response = requests.post(GRAPHQL_URL, headers=HEADERS, json=payload)
        response.raise_for_status()
        data = response.json()
        context_id = data["data"]["viewer"]["project"]["currentEditingContext"]["id"]
        print(f"EditingContextId: {context_id}")
        return context_id
    except requests.exceptions.RequestException as e:
        print(f"Error while fetching EditingContextId: {e}")
        if hasattr(e, 'response') and e.response is not None:
            print(f"Server response: {e.response.text}")
        raise
    except Exception as e:
        print("Unexpected error while parsing response:", data)
        raise

def create_document(editing_context_id, name="Fischertechnik_Warehouse"):
    """
    Creates a new document in the given editing context.
    """
    print("[3] Creating new document...")

    document_id = str(uuid.uuid4()) 

    query = '''
    mutation createDocument($input: CreateDocumentInput!) {
      createDocument(input: $input) {
        __typename
        ... on ErrorPayload {
          message
        }
      }
    }
    '''

    variables = {
        "input": {
            "id": document_id,
            "editingContextId": editing_context_id,
            "stereotypeId": "empty_sysmlv2",
            "name": name
        }
    }

    payload = {
        "operationName": "createDocument",
        "query": query,
        "variables": variables
    }

    try:
        response = requests.post(GRAPHQL_URL, headers=HEADERS, json=payload)
        response.raise_for_status()
        result = response.json()

        typename = result["data"]["createDocument"]["__typename"]

        if typename == "CreateDocumentSuccessPayload":
            print(f"Document created successfully (ID: {document_id})")
            return document_id
        elif typename == "ErrorPayload":
            error_message = result["data"]["createDocument"].get("message", "Unknown error")
            print(f"Error while creating document: {error_message}")
            return None
        else:
            print("Unexpected response type:", typename)
            return None

    except requests.exceptions.RequestException as e:
        print(f"Error while creating document: {e}")
        if hasattr(e, 'response') and e.response is not None:
            print(f"Server response: {e.response.text}")
        raise
    
def insert_textual_sysmlv2(editing_context_id, object_id, textual_content, project_id):
    print("[4] Inserting textual SysMLv2 model...")
    referer = f"http://localhost:8080/projects/{project_id}/edit?selection={object_id}"

    local_headers = HEADERS.copy()
    local_headers["referer"] = referer

    mutation = "mutation insertTextualSysMLv2($input: InsertTextualSysMLv2Input!) {\n  insertTextualSysMLv2(input: $input) {\n    __typename\n    ... on ErrorPayload {\n      messages {\n        body\n        level\n        __typename\n      }\n      __typename\n    }\n  }\n}"

    variables = {
        "input": {
            "id": str(uuid.uuid4()),
            "editingContextId": editing_context_id,
            "objectId": object_id,
            "textualContent": textual_content
        }
    }

    payload = {
        "operationName": "insertTextualSysMLv2",
        "query": mutation,
        "variables": variables
    }

    try:
        response = requests.post(GRAPHQL_URL, headers=local_headers, json=payload)
        response.raise_for_status()
        result = response.json()
    except Exception as e:
        print("Exception occurred during request:")
        traceback.print_exc()
        return

    if "errors" in result:
        print("Error while inserting (GraphQL Errors):", result["errors"])
    elif "data" in result and result["data"]["insertTextualSysMLv2"]["__typename"] == "SuccessPayload":
        print("Inserted successfully!")
    else:
        print("Error while inserting:")
        print(json.dumps(result, indent=2))

def get_first_package_id_rest(project_id):
    """
    Retrieves the first package ID via the REST API (roots endpoint).
    """
    # Query commit IDs
    commits_url = f"{REST_PROJECT_URL}/{project_id}/commits"
    response = requests.get(commits_url, headers=HEADERS)
    response.raise_for_status()
    commits = response.json()
    if not commits:
        raise Exception("No commits found in project!")
    commit_id = commits[0]["@id"]  # Take the latest commit

    # Query root elements
    roots_url = f"{REST_PROJECT_URL}/{project_id}/commits/{commit_id}/roots"
    response = requests.get(roots_url, headers=HEADERS)
    response.raise_for_status()
    roots = response.json()
    for root in roots:
        if root.get("@type") == "Package":
            print(f"Package found: {root.get('name', '')} (ID: {root['@id']})")
            return root["@id"]
    raise Exception("No package root found!")

def create_view(editing_context_id, package_id, representation_description_id, representation_name="General View"):
    """
    Creates a view (representation) for the given package.
    """
    print("[5] Creating view/representation...")

    view_id = str(uuid.uuid4())

    mutation = (
        "mutation createRepresentation($input: CreateRepresentationInput!) {"
        "  createRepresentation(input: $input) {"
        "    __typename"
        "    ... on CreateRepresentationSuccessPayload {"
        "      representation {"
        "        id"
        "        __typename"
        "      }"
        "      __typename"
        "    }"
        "    ... on ErrorPayload {"
        "      message"
        "      __typename"
        "    }"
        "  }"
        "}"
    )

    variables = {
        "input": {
            "id": view_id,
            "editingContextId": editing_context_id,
            "objectId": package_id,
            "representationDescriptionId": representation_description_id,
            "representationName": representation_name
        }
    }

    payload = {
        "operationName": "createRepresentation",
        "query": mutation,
        "variables": variables
    }

    try:
        response = requests.post(GRAPHQL_URL, headers=HEADERS, json=payload)
        response.raise_for_status()
        result = response.json()
        typename = result["data"]["createRepresentation"]["__typename"]

        if typename == "CreateRepresentationSuccessPayload":
            rep_id = result["data"]["createRepresentation"]["representation"]["id"]
            print(f"View/representation created successfully (ID: {rep_id})")
            return rep_id
        elif typename == "ErrorPayload":
            error_message = result["data"]["createRepresentation"].get("message", "Unknown error")
            print(f"Error while creating view: {error_message}")
            return None
        else:
            print("Unexpected response type:", typename)
            return None

    except requests.exceptions.RequestException as e:
        print(f"Error while creating view: {e}")
        if hasattr(e, 'response') and e.response is not None:
            print(f"Server response: {e.response.text}")
        raise

def drop_on_diagram(editing_context_id, representation_id, object_ids, diagram_target_element_id, x=100, y=100):
    """
    Executes the dropOnDiagram operation to drop elements (e.g., Package 1) onto a diagram.
    """
    print("[6] Executing dropOnDiagram...")

    mutation = (
        "mutation dropOnDiagram($input: DropOnDiagramInput!) {"
        "  dropOnDiagram(input: $input) {"
        "    __typename"
        "    ... on DropOnDiagramSuccessPayload {"
        "      diagram {"
        "        id"
        "        __typename"
        "      }"
        "      messages {"
        "        body"
        "        level"
        "        __typename"
        "      }"
        "      __typename"
        "    }"
        "    ... on ErrorPayload {"
        "      messages {"
        "        body"
        "        level"
        "        __typename"
        "      }"
        "      __typename"
        "    }"
        "  }"
        "}"
    )

    variables = {
        "input": {
            "id": str(uuid.uuid4()),
            "editingContextId": editing_context_id,
            "representationId": representation_id,
            "objectIds": object_ids if isinstance(object_ids, list) else [object_ids],
            "startingPositionX": x,
            "startingPositionY": y,
            "diagramTargetElementId": diagram_target_element_id
        }
    }

    payload = {
        "operationName": "dropOnDiagram",
        "query": mutation,
        "variables": variables
    }

    try:
        response = requests.post(GRAPHQL_URL, headers=HEADERS, json=payload)
        response.raise_for_status()
        result = response.json()
        typename = result["data"]["dropOnDiagram"]["__typename"]

        if typename == "DropOnDiagramSuccessPayload":
            diagram = result["data"]["dropOnDiagram"]["diagram"]
            print(f"Element(s) successfully placed on diagram (Diagram ID: {diagram['id']})")
            return diagram["id"]
        elif typename == "ErrorPayload":
            messages = result["data"]["dropOnDiagram"].get("messages", [])
            print("Error during dropOnDiagram:")
            for msg in messages:
                print(f"- {msg.get('level', '')}: {msg.get('body', '')}")
            return None
        else:
            print("Unexpected response type:", typename)
            return None

    except requests.exceptions.RequestException as e:
        print(f"Error during dropOnDiagram: {e}")
        if hasattr(e, 'response') and e.response is not None:
            print(f"Server response: {e.response.text}")
        raise

def invoke_single_click_on_diagram_element_tool(
    editing_context_id,
    representation_id,
    diagram_element_id,
    tool_id,
    x=100,
    y=100,
    variables_list=None
):
    """
    Executes the invokeSingleClickOnDiagramElementTool operation to, e.g., display all elements in the package.
    """
    print("[7] Executing invokeSingleClickOnDiagramElementTool...")

    mutation = (
        "mutation invokeSingleClickOnDiagramElementTool($input: InvokeSingleClickOnDiagramElementToolInput!) {"
        "  invokeSingleClickOnDiagramElementTool(input: $input) {"
        "    __typename"
        "    ... on InvokeSingleClickOnDiagramElementToolSuccessPayload {"
        "      newSelection {"
        "        entries {"
        "          id"
        "          __typename"
        "        }"
        "        __typename"
        "      }"
        "      messages {"
        "        body"
        "        level"
        "        __typename"
        "      }"
        "      __typename"
        "    }"
        "    ... on ErrorPayload {"
        "      messages {"
        "        body"
        "        level"
        "        __typename"
        "      }"
        "      __typename"
        "    }"
        "  }"
        "}"
    )

    variables = {
        "input": {
            "id": str(uuid.uuid4()),
            "editingContextId": editing_context_id,
            "representationId": representation_id,
            "diagramElementId": diagram_element_id,
            "toolId": tool_id,
            "startingPositionX": x,
            "startingPositionY": y,
            "variables": variables_list if variables_list is not None else []
        }
    }

    payload = {
        "operationName": "invokeSingleClickOnDiagramElementTool",
        "query": mutation,
        "variables": variables
    }

    try:
        response = requests.post(GRAPHQL_URL, headers=HEADERS, json=payload)
        response.raise_for_status()
        result = response.json()
        typename = result["data"]["invokeSingleClickOnDiagramElementTool"]["__typename"]

        if typename == "InvokeSingleClickOnDiagramElementToolSuccessPayload":
            print("Elements placed successfully!")
            return result["data"]["invokeSingleClickOnDiagramElementTool"]
        elif typename == "ErrorPayload":
            messages = result["data"]["invokeSingleClickOnDiagramElementTool"].get("messages", [])
            print("Error during tool invoke:")
            for msg in messages:
                print(f"- {msg.get('level', '')}: {msg.get('body', '')}")
            return None
        else:
            print("Unexpected response type:", typename)
            return None

    except requests.exceptions.RequestException as e:
        print(f"Error during invokeSingleClickOnDiagramElementTool: {e}")
        if hasattr(e, 'response') and e.response is not None:
            print(f"Server response: {e.response.text}")
        raise

def delete_autogenerated_projects():
    # Query all projects
    response = requests.get(REST_PROJECT_URL, headers=HEADERS)
    response.raise_for_status()
    projects = response.json()
    print(f"[0] Checking {len(projects)} existing projects for deletion criteria...")

    for project in projects:
        project_name = project.get("name", "")
        project_id = project["@id"]
        if project_name.startswith("AutoCreatedProject"):
            del_url = f"{REST_PROJECT_URL}/{project_id}"
            del_response = requests.delete(del_url, headers=HEADERS)
            if del_response.status_code == 200:
                print(f"Project {project_id} ('{project_name}') deleted successfully.")
            else:
                print(f"Error deleting project {project_id}: {del_response.status_code} - {del_response.text}")
        else:
            print(f"Project {project_id} ('{project_name}') will be kept.")

def close_edge_browser():
    """
    Closes all running Microsoft Edge windows.
    """
    try:
        subprocess.run(["taskkill", "/F", "/IM", "msedge.exe"], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        print("All Edge windows have been closed.")
    except subprocess.CalledProcessError:
        print("Edge was not open or could not be closed.")

def open_in_edge(url):
    edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
    if os.path.exists(edge_path):
        subprocess.Popen([edge_path, "--new-window", f"--app={url}"])
    else:
        print("Edge not found, opening in default browser.")
        webbrowser.open(url)

def move_and_resize_window_by_title(title, x, y, width, height, max_wait=7):
    """
    Searches for a window with the given title and moves/resizes it
    as soon as it is visible and movable.
    """
    waited = 0
    while waited < max_wait:
        titles = gw.getAllTitles()
        for w in titles:
            if title in w:
                win = gw.getWindowsWithTitle(w)[0]
                # Check if the window is visible
                if hasattr(win, "isVisible") and not win.isVisible:
                    continue
                try:
                    win.moveTo(x, y)
                    win.resizeTo(width, height)
                    print(f"Window '{w}' moved and resized.")
                    return True
                except Exception as e:
                    print(f"Window found but not movable yet: {e}")
                    # Do NOT return, keep trying!
        time.sleep(0.1)
        waited += 0.1
    print(f"No window with title '{title}' found for moving/resizing!")
    return False

# Call at the very beginning:
delete_autogenerated_projects()

if __name__ == "__main__":
    # Step 0: Delete all existing projects
    delete_autogenerated_projects()

    # Step 1: Create project (REST)
    project_name = f"AutoCreatedProject_{uuid.uuid4().hex[:8]}"
    project_id = create_project(project_name, "Generated via REST + GraphQL")

    # Step 2: Query EditingContextId (GRAPHQL)
    editing_context_id = get_editing_context_id(project_id)

    # Step 3: Create document (GRAPHQL)
    document_id = create_document(editing_context_id)

    # Step 4: Query first package in project (REST)
    package_id = get_first_package_id_rest(project_id)

    # Step 5: Insert textual SysMLv2 model (GRAPHQL)
    insert_textual_sysmlv2(editing_context_id, package_id, textual_model, project_id)

    # Step 6: Create view (GRAPHQL)
    # The representation_description_id must be known. It can be hardcoded or queried before.
    representation_description_id = "siriusComponents://representationDescription?kind=diagramDescription&sourceKind=view&sourceId=8dcd14b0-6259-3193-ad2c-743f394c68e4&sourceElementId=db495705-e917-319b-af55-a32ad63f4089"
    representation_id = create_view(editing_context_id, package_id, representation_description_id)

    # Step 7: Drop elements on diagram (GRAPHQL)
    # package_id = ID of Package 1
    diagram_element_id = drop_on_diagram(editing_context_id, representation_id, [package_id], package_id, x=100, y=100)

    # Step 8: Apply tool on diagram element (GRAPHQL)
    tool_id = "97fe16fc-349b-3008-8d27-ba323c129cec"
    invoke_single_click_on_diagram_element_tool(
        editing_context_id,
        representation_id,
        diagram_element_id, 
        tool_id,
        x=100,
        y=100
    )
    print("Tool executed successfully!")

    # Step 9: Open diagram in browser
    url = f"http://localhost:8080/projects/{project_id}/edit/{representation_id}?selection={representation_id}"
    print(f"Opening diagram in browser: {url}")
    # Step 10: Close Edge
    close_edge_browser()
    time.sleep(1)


    # Step 11: Open window and move/resize
    user32 = ctypes.windll.user32
    screen_width = user32.GetSystemMetrics(0)
    screen_height = user32.GetSystemMetrics(1)

    # Calculate position and size for lower third, full width
    x = 0
    width = screen_width
    height = screen_height // 3
    y = screen_height - height

    open_in_edge(url)
    move_and_resize_window_by_title("SysON", x, y, width, height, max_wait=5)