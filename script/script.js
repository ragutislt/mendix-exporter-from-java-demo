"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
const mendixmodelsdk_1 = require("mendixmodelsdk");
const mendixplatformsdk_1 = require("mendixplatformsdk");
const fs = __importStar(require("fs"));
var ImportedAssociationType;
(function (ImportedAssociationType) {
    ImportedAssociationType["ONE_TO_ONE"] = "ONE_TO_ONE";
    ImportedAssociationType["ONE_TO_MANY"] = "ONE_TO_MANY";
})(ImportedAssociationType || (ImportedAssociationType = {}));
var ImportedAttributeType;
(function (ImportedAttributeType) {
    ImportedAttributeType["INTEGER"] = "INTEGER";
    ImportedAttributeType["STRING"] = "STRING";
    ImportedAttributeType["DECIMAL"] = "DECIMAL";
    ImportedAttributeType["AUTO_NUMBER"] = "AUTO_NUMBER";
    ImportedAttributeType["BOOLEAN"] = "BOOLEAN";
    ImportedAttributeType["ENUM"] = "ENUM";
    ImportedAttributeType["ENTITY"] = "ENTITY";
})(ImportedAttributeType || (ImportedAttributeType = {}));
async function main(dbEntitiesFile, restServicesFile, appId) {
    const client = new mendixplatformsdk_1.MendixPlatformClient();
    const app = await client.getApp(appId);
    const workingCopy = await app.createTemporaryWorkingCopy("main");
    const model = await workingCopy.openModel();
    const domainModelInterface = model.allDomainModels().filter(dm => dm.containerAsModule.name === "MyFirstModule")[0];
    const domainModel = await domainModelInterface.load();
    const module = model.allModules().filter(m => m.name === "MyFirstModule")[0];
    //const module = model.allFolderBases().filter(b => b.name === "MyFirstModule");
    //model.allFolderBases()
    const dbEntitiesJson = fs.readFileSync(dbEntitiesFile, 'utf-8');
    const restServicesJson = fs.readFileSync(restServicesFile, 'utf-8');
    createMendixEntities(domainModel, dbEntitiesJson);
    createMendixRestServices(module, restServicesJson);
    await commitChanges(model, workingCopy, dbEntitiesFile);
}
function createMendixEntities(domainModel, entitiesInJson) {
    const importedEntities = JSON.parse(entitiesInJson);
    const entitiesToCreate = importedEntities.filter(e => !entityAlreadyExists(e, domainModel));
    entitiesToCreate.forEach((importedEntity, i) => {
        console.info(`Hello from ${importedEntity.name}`);
        const mendixEntity = mendixmodelsdk_1.domainmodels.Entity.createIn(domainModel);
        mendixEntity.name = importedEntity.name;
        mendixEntity.location = { x: 100 + i * 300, y: 50 };
        processAttributes(importedEntity, mendixEntity);
    });
    entitiesToCreate.forEach(importedEntity => {
        const mendixParentEntity = domainModel.entities.find(e => e.name === importedEntity.name);
        processAssociations(importedEntity, domainModel, mendixParentEntity);
    });
}
function processAssociations(importedEntity, domainModel, mendixParentEntity) {
    importedEntity.attributes.filter(a => a.type === ImportedAttributeType.ENTITY).forEach((a, index) => {
        const mendixAssociation = mendixmodelsdk_1.domainmodels.Association.createIn(domainModel);
        const mendixChildEntity = domainModel.entities.find(e => e.name === a.entityType);
        mendixAssociation.name = `${mendixParentEntity?.name}_${mendixChildEntity?.name}`;
        mendixAssociation.parent = mendixParentEntity;
        mendixAssociation.child = mendixChildEntity;
        mendixAssociation.parentConnection.x = 100;
        mendixAssociation.parentConnection.y = 50;
        mendixAssociation.type = a.associationType === ImportedAssociationType.ONE_TO_ONE || a.associationType === ImportedAssociationType.ONE_TO_MANY ?
            mendixmodelsdk_1.domainmodels.AssociationType.Reference : mendixmodelsdk_1.domainmodels.AssociationType.ReferenceSet;
        mendixAssociation.owner = a.associationType === ImportedAssociationType.ONE_TO_ONE ? mendixmodelsdk_1.domainmodels.AssociationOwner.Both : mendixmodelsdk_1.domainmodels.AssociationOwner.Default;
    });
}
function processAttributes(importedEntity, mendixEntity) {
    importedEntity.attributes.filter(a => a.type !== ImportedAttributeType.ENTITY).forEach(a => {
        const mendixAttribute = mendixmodelsdk_1.domainmodels.Attribute.createIn(mendixEntity);
        mendixAttribute.name = capitalize(getAttributeName(a.name, importedEntity));
        mendixAttribute.type = assignAttributeType(a.type, mendixAttribute);
    });
}
function entityAlreadyExists(importedEntity, domainModel) {
    return domainModel.entities.find(e => e.name === importedEntity.name);
}
function getAttributeName(importedAttributeName, mendixEntity) {
    return importedAttributeName === "id" ? `${mendixEntity.name}Id` : importedAttributeName;
}
async function commitChanges(model, workingCopy, entitiesFile) {
    await model.flushChanges();
    await workingCopy.commitToRepository("main", { commitMessage: `Imported DB entities from ${entitiesFile}` });
}
function assignAttributeType(type, attribute) {
    switch (type) {
        case ImportedAttributeType.INTEGER:
            return mendixmodelsdk_1.domainmodels.IntegerAttributeType.createInAttributeUnderType(attribute);
        case ImportedAttributeType.STRING:
            return mendixmodelsdk_1.domainmodels.StringAttributeType.createInAttributeUnderType(attribute);
        case ImportedAttributeType.DECIMAL:
            return mendixmodelsdk_1.domainmodels.DecimalAttributeType.createInAttributeUnderType(attribute);
        case ImportedAttributeType.AUTO_NUMBER:
            mendixmodelsdk_1.domainmodels.StoredValue.createIn(attribute).defaultValue = "1";
            return mendixmodelsdk_1.domainmodels.AutoNumberAttributeType.createInAttributeUnderType(attribute);
        case ImportedAttributeType.BOOLEAN:
            return mendixmodelsdk_1.domainmodels.BooleanAttributeType.createInAttributeUnderType(attribute);
        case ImportedAttributeType.ENUM:
            return mendixmodelsdk_1.domainmodels.EnumerationAttributeType.createInAttributeUnderType(attribute);
        default:
            throw new Error(`attribute ${attribute.name} did not have a valid mendix type defined`);
    }
}
function createMendixRestServices(module, restServicesJson) {
    const importedServices = JSON.parse(restServicesJson);
    importedServices.forEach(importedService => {
        console.info(`Hello from ${importedService.serviceName}`);
        const restService = mendixmodelsdk_1.rest.PublishedRestService.createIn(module);
        restService.serviceName = importedService.serviceName;
        restService.path = importedService.path;
        restService.version = importedService.version;
        restService.name = importedService.serviceName;
        // importedService.resources.forEach(importedResource => {
        //     const publishedRestResource = rest.PublishedRestServiceResource.createIn(restService);
        //     importedResource.operations.forEach(op => {
        //         const publishedRestOperation = rest.PublishedRestServiceOperation.createIn(publishedRestResource);
        //         publishedRestOperation.path = op.path;
        //         publishedRestOperation.httpMethod = getMendixHttpMethod(op.restOperation);
        //     });
        // });
    });
}
function getMendixHttpMethod(importedOperationMethod) {
    switch (importedOperationMethod) {
        case "GET":
            return mendixmodelsdk_1.services.HttpMethod.Get;
        case "POST":
            return mendixmodelsdk_1.services.HttpMethod.Post;
        case "PUT":
            return mendixmodelsdk_1.services.HttpMethod.Put;
        case "DELETE":
            return mendixmodelsdk_1.services.HttpMethod.Delete;
        default:
            throw new Error(`imported http method ${importedOperationMethod} did not have a valid method in mendix defined`);
    }
}
function capitalize(word) {
    return word.charAt(0).toUpperCase() + word.slice(1);
}
main(process.argv[2], process.argv[3], process.argv[4]).catch(console.error);
//# sourceMappingURL=script.js.map