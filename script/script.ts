import { domainmodels, IModel } from "mendixmodelsdk";
import { MendixPlatformClient, OnlineWorkingCopy } from "mendixplatformsdk";
import * as fs from 'fs';

interface ImportedEntity {
    name: string;
    generalization: string;
    attributes: ImportedAttribute[];
}

interface ImportedAttribute {
    name: string;
    type: ImportedAttributeType;
    entityType: string;
    associationType: ImportedAssociationType;
}

enum ImportedAssociationType {
    ONE_TO_ONE = "ONE_TO_ONE",
    ONE_TO_MANY = "ONE_TO_MANY"
}

enum ImportedAttributeType {
    INTEGER = "INTEGER",
    STRING = "STRING",
    DECIMAL = "DECIMAL",
    AUTO_NUMBER = "AUTO_NUMBER",
    BOOLEAN = "BOOLEAN",
    ENUM = "ENUM",
    ENTITY = "ENTITY"
}

async function main(dbEntitiesFile: string, appId: string) {
    const client = new MendixPlatformClient();
    const app = await client.getApp(appId);

    const workingCopy = await app.createTemporaryWorkingCopy("main");
    const model = await workingCopy.openModel();

    const domainModelInterface = model.allDomainModels().filter(dm => dm.containerAsModule.name === "MyFirstModule")[0];
    const domainModel = await domainModelInterface.load();

    const dbEntitiesJson: string = fs.readFileSync(dbEntitiesFile, 'utf-8');

    createMendixEntities(domainModel, dbEntitiesJson);

    await commitChanges(model, workingCopy, dbEntitiesFile);
}

function createMendixEntities(domainModel: domainmodels.DomainModel, entitiesInJson: any) {
    const importedEntities: ImportedEntity[] = JSON.parse(entitiesInJson);

    importedEntities.forEach((importedEntity, i) => {
        console.info(`Hello from ${importedEntity.name}`);

        const mendixEntity = domainmodels.Entity.createIn(domainModel);
        mendixEntity.name = importedEntity.name;
        mendixEntity.location = { x: 100 + i * 300, y: 50 };

        processAttributes(importedEntity, mendixEntity);
    });

    importedEntities.forEach(importedEntity => {
        const mendixParentEntity = domainModel.entities.find(e => e.name === importedEntity.name) as domainmodels.Entity;
        processAssociations(importedEntity, domainModel, mendixParentEntity);
    });
}

function processAssociations(importedEntity: ImportedEntity, domainModel: domainmodels.DomainModel, mendixParentEntity: domainmodels.Entity) {
    importedEntity.attributes.filter(a => a.type === ImportedAttributeType.ENTITY).forEach((a, index) => {
        const mendixAssociation = domainmodels.Association.createIn(domainModel);
        const mendixChildEntity = domainModel.entities.find(e => e.name === a.entityType) as domainmodels.Entity;

        mendixAssociation.name = `${mendixParentEntity?.name}_${mendixChildEntity?.name}`;
        mendixAssociation.parent = mendixParentEntity;
        mendixAssociation.child = mendixChildEntity;
        mendixAssociation.parentConnection.x = 100;
        mendixAssociation.parentConnection.y = 50;
        mendixAssociation.type = a.associationType === ImportedAssociationType.ONE_TO_ONE || a.associationType === ImportedAssociationType.ONE_TO_MANY ?
            domainmodels.AssociationType.Reference : domainmodels.AssociationType.ReferenceSet;
        mendixAssociation.owner = a.associationType === ImportedAssociationType.ONE_TO_ONE ? domainmodels.AssociationOwner.Both : domainmodels.AssociationOwner.Default;
    });
}

function processAttributes(importedEntity: ImportedEntity, mendixEntity: domainmodels.Entity) {
    importedEntity.attributes.filter(a => a.type !== ImportedAttributeType.ENTITY).forEach(a => {
        const mendixAttribute = domainmodels.Attribute.createIn(mendixEntity);
        mendixAttribute.name = capitalize(getAttributeName(a.name, importedEntity));
        mendixAttribute.type = assignAttributeType(a.type, mendixAttribute);
    });
}

function getAttributeName(importedAttributeName: string, mendixEntity: ImportedEntity): string {
    return importedAttributeName === "id" ? `${mendixEntity.name}Id` : importedAttributeName;
}

async function commitChanges(model: IModel, workingCopy: OnlineWorkingCopy, entitiesFile: string) {
    await model.flushChanges();
    await workingCopy.commitToRepository("main", { commitMessage: `Imported DB entities from ${entitiesFile}` });
}

function assignAttributeType(type: ImportedAttributeType, attribute: domainmodels.Attribute): domainmodels.AttributeType {
    switch (type as ImportedAttributeType) {
        case ImportedAttributeType.INTEGER:
            return domainmodels.IntegerAttributeType.createInAttributeUnderType(attribute);
        case ImportedAttributeType.STRING:
            return domainmodels.StringAttributeType.createInAttributeUnderType(attribute);
        case ImportedAttributeType.DECIMAL:
            return domainmodels.DecimalAttributeType.createInAttributeUnderType(attribute);
        case ImportedAttributeType.AUTO_NUMBER:
            domainmodels.StoredValue.createIn(attribute).defaultValue = "1";
            return domainmodels.AutoNumberAttributeType.createInAttributeUnderType(attribute);
        case ImportedAttributeType.BOOLEAN:
            return domainmodels.BooleanAttributeType.createInAttributeUnderType(attribute);
        case ImportedAttributeType.ENUM:
            return domainmodels.EnumerationAttributeType.createInAttributeUnderType(attribute);
        default:
            throw new Error(`attribute ${attribute.name} did not have a valid mendix type defined`);
    }
}

function capitalize(word: string): string {
    return word.charAt(0).toUpperCase() + word.slice(1);
}


main(process.argv[2], process.argv[3]).catch(console.error);
